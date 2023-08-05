package su.plo.voice.proto.packets.udp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketUtil;
import su.plo.voice.proto.packets.udp.bothbound.BaseAudioPacket;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString(callSuper = true)
public final class SourceAudioPacket extends BaseAudioPacket<ClientPacketUdpHandler> {

    @Getter
    private UUID sourceId;
    @Getter
    @Setter
    private byte sourceState;
    @Getter
    @Setter
    private short distance;

    public SourceAudioPacket(long sequenceNumber, byte sourceState, byte[] data, @NotNull UUID sourceId, short distance) {
        super(sequenceNumber, data);
        this.sourceId = sourceId;
        this.sourceState = sourceState;
        this.distance = distance;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        super.read(in);

        this.sourceId = PacketUtil.readUUID(in);
        this.sourceState = in.readByte();
        this.distance = in.readShort();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        super.write(out);

        PacketUtil.writeUUID(out, checkNotNull(sourceId, "sourceId"));
        out.writeByte(sourceState);
        out.writeShort(distance);
    }

    @Override
    public void handle(ClientPacketUdpHandler handler) {
        handler.handle(this);
    }
}
