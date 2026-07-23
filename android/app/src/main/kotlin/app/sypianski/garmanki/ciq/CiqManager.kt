package app.sypianski.garmanki.ciq

import android.content.Context
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface CiqState {
    data object Uninitialized : CiqState
    data object Initializing : CiqState
    data object Ready : CiqState
    data class Error(val message: String) : CiqState
}

data class WatchDevice(
    val id: Long,
    val name: String,
    val status: IQDevice.IQDeviceStatus,
    /** null = unknown / query in flight */
    val appInstalled: Boolean?,
) {
    val connected: Boolean get() = status == IQDevice.IQDeviceStatus.CONNECTED
}

sealed interface PushStatus {
    data object Idle : PushStatus
    data class Sending(val seq: Int, val of: Int) : PushStatus
    data object AwaitingAck : PushStatus
    data class Done(val rev: Int) : PushStatus
    data class Failed(val reason: PushFailure, val detail: String? = null) : PushStatus
}

enum class PushFailure { NO_TARGET, SEND, ACK_TIMEOUT, NACK, SDK, BUSY }

/** Messages the watch sends us — SCHEMA.md §3. */
sealed interface WatchMessage {
    /** `pv` = watch's protocol/contract version (SCHEMA.md §1); null if omitted. */
    data class Hello(val rev: Int?, val pend: Int, val pv: Int? = null) : WatchMessage
    data class Answers(
        val batch: Int,
        val ans: List<List<Any?>>,
        val act: List<List<Any?>>,
    ) : WatchMessage
}

/** Seam for SyncEngine tests — CiqManager is the real implementation. */
interface CiqLink {
    val messages: SharedFlow<WatchMessage>
    suspend fun push(chunks: List<Map<String, Any?>>, rev: Int): PushStatus
    fun send(message: Map<String, Any?>)
}

/**
 * Wraps the callback-based Connect IQ Mobile SDK into flows. Every SDK call
 * is guarded — failures surface as state, never as crashes (the SDK throws
 * InvalidState/ServiceUnavailable liberally when GCM is missing, restarting,
 * or the binder drops). Pattern mirrored from notes_and_codes.
 */
class CiqManager(private val appContext: Context) : CiqLink {

    private val _state = MutableStateFlow<CiqState>(CiqState.Uninitialized)
    val state: StateFlow<CiqState> = _state.asStateFlow()

    private val _devices = MutableStateFlow<List<WatchDevice>>(emptyList())
    val devices: StateFlow<List<WatchDevice>> = _devices.asStateFlow()

    private val _pushStatus = MutableStateFlow<PushStatus>(PushStatus.Idle)
    val pushStatus: StateFlow<PushStatus> = _pushStatus.asStateFlow()

    private val _messages = MutableSharedFlow<WatchMessage>(extraBufferCapacity = 16)
    override val messages: SharedFlow<WatchMessage> = _messages.asSharedFlow()

    private var connectIQ: ConnectIQ? = null
    private var knownDevices: List<IQDevice> = emptyList()
    private var pushing = false

    /** Ack of the state rev currently awaited by push(), if any. */
    private var pendingAck: Pair<Int, CompletableDeferred<Pair<Boolean, String?>>>? = null

    /**
     * One listener for everything the watch app sends: state acks are routed
     * to the in-flight push, hello/answers surface on [messages].
     */
    private val appEventListener = ConnectIQ.IQApplicationEventListener { _, _, data, _ ->
        data.orEmpty().filterIsInstance<Map<*, *>>().forEach { onWatchMap(it) }
    }

    private fun onWatchMap(map: Map<*, *>) {
        if (asInt(map["p"]) != 1) return
        val ackRev = asInt(map["ack"])
        if (ackRev != null) {
            val pending = pendingAck
            if (pending != null && pending.first == ackRev) {
                pending.second.complete((map["ok"] as? Boolean ?: false) to (map["err"] as? String))
            }
            return
        }
        when (map["t"]) {
            "h" -> _messages.tryEmit(
                WatchMessage.Hello(
                    rev = asInt(map["rev"]),
                    pend = asInt(map["pend"]) ?: 0,
                    pv = asInt(map["pv"]),
                )
            )
            "a" -> {
                val batch = asInt(map["batch"]) ?: return
                _messages.tryEmit(
                    WatchMessage.Answers(
                        batch = batch,
                        ans = asRows(map["ans"]),
                        act = asRows(map["act"]),
                    )
                )
            }
        }
    }

    /**
     * Idempotent. [uiContext] should be an Activity so the SDK's autoUI
     * install/upgrade dialogs can show.
     */
    fun initialize(uiContext: Context) {
        val current = _state.value
        if (current is CiqState.Ready || current is CiqState.Initializing) return
        _state.value = CiqState.Initializing
        try {
            val ciq = ConnectIQ.getInstance(uiContext, ConnectIQ.IQConnectType.WIRELESS)
            connectIQ = ciq
            ciq.initialize(uiContext, true, object : ConnectIQ.ConnectIQListener {
                override fun onSdkReady() {
                    _state.value = CiqState.Ready
                    refreshDevices()
                }

                override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus?) {
                    _state.value = CiqState.Error(status?.name ?: "UNKNOWN")
                }

                override fun onSdkShutDown() {
                    _state.value = CiqState.Uninitialized
                    _devices.value = emptyList()
                }
            })
        } catch (t: Throwable) {
            _state.value = CiqState.Error(t.message ?: t.javaClass.simpleName)
        }
    }

    fun refreshDevices() {
        val ciq = readyCiq() ?: return
        try {
            val known = ciq.knownDevices ?: emptyList()
            knownDevices = known
            val connectedIds = try {
                (ciq.connectedDevices ?: emptyList()).map { it.deviceIdentifier }.toSet()
            } catch (t: Throwable) {
                emptySet()
            }
            _devices.value = known.map { d ->
                val status = if (d.deviceIdentifier in connectedIds) {
                    IQDevice.IQDeviceStatus.CONNECTED
                } else {
                    runCatching { ciq.getDeviceStatus(d) }.getOrNull()
                        ?: IQDevice.IQDeviceStatus.UNKNOWN
                }
                WatchDevice(d.deviceIdentifier, d.friendlyName ?: "Garmin", status, null)
            }
            val app = IQApp(WATCH_APP_UUID)
            known.forEach { d ->
                runCatching {
                    ciq.registerForDeviceEvents(d) { dev, status ->
                        updateDevice(dev.deviceIdentifier) { it.copy(status = status) }
                    }
                }
                // Permanent app-event listener: hello/answers can arrive at
                // any moment while the process is alive (SCHEMA.md §7).
                runCatching { ciq.registerForAppEvents(d, app, appEventListener) }
                queryAppInstalled(ciq, d)
            }
        } catch (t: Throwable) {
            _state.value = CiqState.Error(t.message ?: t.javaClass.simpleName)
        }
    }

    private fun queryAppInstalled(ciq: ConnectIQ, device: IQDevice) {
        try {
            ciq.getApplicationInfo(WATCH_APP_UUID, device,
                object : ConnectIQ.IQApplicationInfoListener {
                    override fun onApplicationInfoReceived(app: IQApp?) {
                        updateDevice(device.deviceIdentifier) { it.copy(appInstalled = true) }
                    }

                    override fun onApplicationNotInstalled(applicationId: String?) {
                        updateDevice(device.deviceIdentifier) { it.copy(appInstalled = false) }
                    }
                })
        } catch (t: Throwable) {
            // leave appInstalled = null (unknown)
        }
    }

    private fun updateDevice(id: Long, transform: (WatchDevice) -> WatchDevice) {
        _devices.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }

    /** True when a push can go somewhere right now. */
    fun canPush(): Boolean =
        _state.value is CiqState.Ready &&
            _devices.value.any { it.connected && it.appInstalled != false }

    /**
     * SCHEMA.md §4: chunks sent sequentially, each awaiting SUCCESS; then wait
     * for the watch ack {p:1, ack:rev, ok:…}. Never throws — the outcome
     * lands in [pushStatus] and is returned.
     */
    override suspend fun push(chunks: List<Map<String, Any?>>, rev: Int): PushStatus {
        if (pushing) return PushStatus.Failed(PushFailure.BUSY)
        val ciq = readyCiq() ?: return fail(PushFailure.SDK, "SDK not ready")
        val target = pickTarget() ?: return fail(PushFailure.NO_TARGET)

        pushing = true
        val app = IQApp(WATCH_APP_UUID)
        val ack = CompletableDeferred<Pair<Boolean, String?>>()
        pendingAck = rev to ack
        try {
            for ((idx, chunk) in chunks.withIndex()) {
                _pushStatus.value = PushStatus.Sending(idx + 1, chunks.size)
                val status = sendChunk(ciq, target, app, chunk)
                    ?: return fail(PushFailure.SEND, "chunk ${idx + 1}/${chunks.size}: send threw")
                if (status != ConnectIQ.IQMessageStatus.SUCCESS) {
                    return fail(PushFailure.SEND, "chunk ${idx + 1}/${chunks.size}: ${status.name}")
                }
            }
            _pushStatus.value = PushStatus.AwaitingAck
            val result = withTimeoutOrNull(ACK_TIMEOUT_MS) { ack.await() }
            return when {
                result == null -> fail(PushFailure.ACK_TIMEOUT)
                result.first -> PushStatus.Done(rev).also { _pushStatus.value = it }
                else -> fail(PushFailure.NACK, result.second)
            }
        } finally {
            pushing = false
            pendingAck = null
            val s = _pushStatus.value
            if (s is PushStatus.Sending || s == PushStatus.AwaitingAck) {
                _pushStatus.value = PushStatus.Idle
            }
        }
    }

    /** Fire-and-forget single message (answers-ack). */
    override fun send(message: Map<String, Any?>) {
        val ciq = readyCiq() ?: return
        val target = pickTarget() ?: return
        try {
            ciq.sendMessage(target, IQApp(WATCH_APP_UUID), message) { _, _, _ -> }
        } catch (t: Throwable) {
            // lost — the watch re-flushes with the same batch id
        }
    }

    private fun pickTarget(): IQDevice? {
        // appInstalled may still be null (query in flight) right after init —
        // don't block on it, only skip devices confirmed app-less.
        val info = _devices.value.firstOrNull { it.connected && it.appInstalled != false }
            ?: return null
        return knownDevices.firstOrNull { it.deviceIdentifier == info.id }
    }

    private suspend fun sendChunk(
        ciq: ConnectIQ,
        device: IQDevice,
        app: IQApp,
        chunk: Map<String, Any?>,
    ): ConnectIQ.IQMessageStatus? = suspendCancellableCoroutine { cont ->
        try {
            ciq.sendMessage(device, app, chunk) { _, _, status ->
                if (cont.isActive) cont.resume(status)
            }
        } catch (t: Throwable) {
            if (cont.isActive) cont.resume(null)
        }
    }

    private fun fail(reason: PushFailure, detail: String? = null): PushStatus.Failed =
        PushStatus.Failed(reason, detail).also { _pushStatus.value = it }

    private fun readyCiq(): ConnectIQ? =
        if (_state.value is CiqState.Ready) connectIQ else null

    fun shutdown() {
        runCatching { connectIQ?.shutdown(appContext) }
        connectIQ = null
        _state.value = CiqState.Uninitialized
        _devices.value = emptyList()
    }

    companion object {
        private const val ACK_TIMEOUT_MS = 30_000L

        private fun asInt(v: Any?): Int? = (v as? Number)?.toInt()

        private fun asRows(v: Any?): List<List<Any?>> =
            (v as? List<*>)?.filterIsInstance<List<Any?>>() ?: emptyList()
    }
}
