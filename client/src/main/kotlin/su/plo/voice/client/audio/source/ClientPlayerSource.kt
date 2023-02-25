package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import su.plo.config.entry.BooleanConfigEntry
import su.plo.config.entry.DoubleConfigEntry
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket

class ClientPlayerSource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: PlayerSourceInfo
) : BaseClientAudioSource<PlayerSourceInfo>(voiceClient, config, sourceInfo) {

    override var sourceVolume: DoubleConfigEntry = config.voice
        .volumes
        .getVolume("source_${sourceInfo.playerInfo.playerId}")

    override fun process(packet: SourceAudioPacket) {
        if (sourceMute.value()) return
        super.process(packet)
    }

    override fun process(packet: SourceAudioEndPacket) {
        if (sourceMute.value()) return
        super.process(packet)
    }

    override fun getPosition(position: FloatArray): FloatArray {
        sourcePlayer?.let { player ->
            position[0] = player.x.toFloat()
            position[1] = (player.y + player.eyeHeight).toFloat()
            position[2] = player.z.toFloat()
        }
        return position
    }

    override fun getLookAngle(lookAngle: FloatArray): FloatArray {
        sourcePlayer?.let { player ->
            val playerLookAngle = player.lookAngle
            lookAngle[0] = playerLookAngle.x.toFloat()
            lookAngle[1] = playerLookAngle.y.toFloat()
            lookAngle[2] = playerLookAngle.z.toFloat()
        }
        return lookAngle
    }

    private val sourceMute: BooleanConfigEntry
        get() {
            return config.voice
                .volumes
                .getMute("source_" + sourceInfo.playerInfo.playerId)
        }

    private val sourcePlayer: Player?
        get() {
            return Minecraft.getInstance().level?.getPlayerByUUID(sourceInfo.playerInfo.playerId)
        }
}