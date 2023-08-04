package su.plo.voice.server.connection;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.server.entity.ModServerPlayer;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.BaseVoiceServer;

import java.util.stream.Collectors;

//#if FABRIC
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
//#else

//#if MC>=11802
//$$ import net.minecraftforge.network.NetworkDirection;
//$$ import net.minecraftforge.network.NetworkEvent;
//#elseif MC>=11701
//$$ import net.minecraftforge.fmllegacy.network.NetworkDirection;
//$$ import net.minecraftforge.fmllegacy.network.NetworkEvent;
//#else
//$$ import net.minecraftforge.fml.network.NetworkDirection;
//$$ import net.minecraftforge.fml.network.NetworkEvent;
//#endif

//$$ import net.minecraft.ResourceLocationException;
//$$ import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
//$$
//$$ import io.netty.util.AsciiString;
//$$ import java.util.ArrayList;
//#endif

import java.io.IOException;
import java.util.List;

public final class ModServerChannelHandler
        extends BaseServerChannelHandler
        //#if FABRIC
        implements ServerPlayNetworking.PlayChannelHandler, S2CPlayChannelEvents.Register
        //#endif
{

    public static ModServerChannelHandler INSTANCE;

    public ModServerChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        super(voiceServer);

        INSTANCE = this;
    }

    @Override
    protected void handleRegisterChannels(List<String> channels, VoiceServerPlayer player) {
        super.handleRegisterChannels(channels, player);
        channels.forEach((channel) ->
                ((ModServerPlayer) player.getInstance()).addChannel(channel)
        );
    }

    private void receive(ServerPlayer player, FriendlyByteBuf buf) {
        byte[] data = ByteBufUtil.getBytes(buf.duplicate());

        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(data))
                    .ifPresent(packet -> {
                        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().wrap(player);

                        PlayerChannelHandler channel = channels.computeIfAbsent(
                                player.getUUID(),
                                (playerId) -> new PlayerChannelHandler(voiceServer, voicePlayer)
                        );

                        channel.handlePacket(packet);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //#if FABRIC
    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        receive(player, buf);
    }

    @Override
    public void onChannelRegister(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server, List<ResourceLocation> channels) {
        voiceServer.getPlayerManager().getPlayerById(handler.player.getUUID()).ifPresent(player ->
                handleRegisterChannels(
                        channels.stream().map(ResourceLocation::toString).collect(Collectors.toList()),
                        player
                )
        );
    }
    //#else
    //$$ public void onChannelRegister(@NotNull ServerPlayer serverPlayer, @NotNull ServerboundCustomPayloadPacket packet) {
    //#if MC>=11701
    //$$     FriendlyByteBuf buf = packet.getData();
    //#else
    //$$     FriendlyByteBuf buf = packet.getInternalData();
    //#endif
    //$$
    //$$     List<ResourceLocation> channels = new ArrayList<>();
    //$$     StringBuilder active = new StringBuilder();
    //$$
    //$$     while (buf.isReadable()) {
    //$$         byte b = buf.readByte();
    //$$
    //$$         if (b != 0) {
    //$$             active.append(AsciiString.b2c(b));
    //$$         } else {
    //$$             try {
    //$$                 channels.add(new ResourceLocation(active.toString()));
    //$$             } catch (ResourceLocationException ex) {
    //$$                 continue;
    //$$             }
    //$$             active = new StringBuilder();
    //$$         }
    //$$     }
    //$$
    //$$     VoiceServerPlayer player = voiceServer.getPlayerManager().wrap(serverPlayer);
    //$$     handleRegisterChannels(
    //$$             channels.stream().map(ResourceLocation::toString).collect(Collectors.toList()),
    //$$             player
    //$$     );
    //$$ }
    //$$
    //$$ public void receive(@NotNull NetworkEvent event) {
    //$$     NetworkEvent.Context context = event.getSource().get();
    //$$     if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER || event.getPayload() == null) return;
    //$$     receive(context.getSender(), event.getPayload());
    //$$     context.setPacketHandled(true);
    //$$ }
    //#endif
}
