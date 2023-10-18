package su.plo.voice.server.audio.line

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.audio.line.BaseServerSourceLine
import su.plo.voice.api.server.audio.line.ServerPlayerSetManager
import su.plo.voice.api.server.audio.source.ServerAudioSource
import su.plo.voice.api.server.audio.source.ServerBroadcastSource
import su.plo.voice.api.server.audio.source.ServerDirectSource
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.line.VoiceSourceLine
import su.plo.voice.server.audio.source.VoiceServerBroadcastSource
import su.plo.voice.server.audio.source.VoiceServerDirectSource
import java.util.*
import java.util.stream.Collectors

abstract class VoiceBaseServerSourceLine(
    protected open val voiceServer: PlasmoBaseVoiceServer,
    override val addon: AddonContainer,
    name: String,
    translation: String,
    icon: String,
    defaultVolume: Double,
    weight: Int,
    withPlayers: Boolean
) : BaseServerSourceLine, VoiceSourceLine(name, translation, icon, defaultVolume, weight, null) {

    override val playerSetManager: ServerPlayerSetManager? =
        if (withPlayers) VoiceServerPlayerSetManager(this) else null

    protected val sourceById: MutableMap<UUID, ServerAudioSource<*>> = Maps.newConcurrentMap()

    override fun getSourceLineForPlayer(player: VoicePlayer): VoiceSourceLine =
        playerSetManager?.let {
            VoiceSourceLine(
                name,
                translation,
                icon,
                defaultVolume,
                weight,
                it.getPlayerSet(player)
                    .players
                    .stream()
                    .map { linePlayer -> linePlayer.instance.gameProfile }
                    .collect(Collectors.toSet())
            ).also {
                players = Sets.newHashSet()
            }
        } ?: this

    override fun createBroadcastSource(stereo: Boolean, decoderInfo: CodecInfo?): ServerBroadcastSource =
        VoiceServerBroadcastSource(
            voiceServer,
            voiceServer.udpConnectionManager,
            addon,
            this,
            decoderInfo,
            stereo
        ).also { sourceById[it.id] = it }

    override fun createDirectSource(player: VoicePlayer, stereo: Boolean, decoderInfo: CodecInfo?): ServerDirectSource =
        VoiceServerDirectSource(
            voiceServer,
            voiceServer.udpConnectionManager,
            addon,
            this,
            decoderInfo,
            stereo,
            player
        ).also { sourceById[it.id] = it }

    override fun removeSource(sourceId: UUID) {
        sourceById.remove(sourceId)
    }

    override fun getSourceById(sourceId: UUID): Optional<ServerAudioSource<*>> =
        Optional.ofNullable(sourceById[sourceId])

    override fun getSources(): MutableCollection<ServerAudioSource<*>> =
        sourceById.values

    override fun clear() = sourceById.clear()
}
