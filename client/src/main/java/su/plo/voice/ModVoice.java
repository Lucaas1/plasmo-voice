package su.plo.voice;

import su.plo.voice.server.ModVoiceServer;
import su.plo.voice.util.version.ModrinthLoader;

// todo legacy forge?
//#if FABRIC
import net.fabricmc.api.ModInitializer;
//#else
//$$ import su.plo.voice.client.ModVoiceClient;
//$$
//$$ import net.minecraftforge.common.MinecraftForge;
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent;
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//$$ import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

//#if MC>=11802
//$$ import net.minecraftforge.network.NetworkRegistry;
//$$ import net.minecraftforge.network.event.EventNetworkChannel;
//#elseif MC>=11701
//$$ import net.minecraftforge.fmllegacy.network.event.EventNetworkChannel;
//$$ import net.minecraftforge.fmllegacy.network.NetworkRegistry;
//#else
//$$ import net.minecraftforge.fml.network.event.EventNetworkChannel;
//$$ import net.minecraftforge.fml.network.NetworkRegistry;
//#endif

//#endif

//#if FORGE
//$$ @Mod("plasmovoice")
//#endif
public final class ModVoice
        //#if FABRIC
        implements ModInitializer
        //#endif
{

    //#if FORGE
    //$$ private EventNetworkChannel channel;
    //$$
    //$$ public ModVoice() {
    //$$     FMLJavaModLoadingContext.get().getModEventBus().register(this);
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onClientSetup(FMLClientSetupEvent event) {
    //$$     ModVoiceClient voiceClient = new ModVoiceClient();
    //$$     MinecraftForge.EVENT_BUS.register(voiceClient);
    //$$     voiceClient.onInitialize(channel);
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onCommonSetup(FMLCommonSetupEvent event) {
    //$$     this.channel = NetworkRegistry.newEventChannel(
    //$$             ModVoiceServer.CHANNEL,
    //$$             () -> NetworkRegistry.ACCEPTVANILLA,
    //$$             NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA),
    //$$             NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA)
    //$$     );
    //$$
    // todo: register forge channels in slib
    //$$     NetworkRegistry.newEventChannel(
    //$$             ModVoiceServer.FLAG_CHANNEL,
    //$$             () -> NetworkRegistry.ACCEPTVANILLA,
    //$$             NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA),
    //$$             NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA)
    //$$     );
    //$$
    //$$     EventNetworkChannel serviceChannel = NetworkRegistry.newEventChannel(
    //$$             ModVoiceServer.SERVICE_CHANNEL,
    //$$             () -> NetworkRegistry.ACCEPTVANILLA,
    //$$             NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA),
    //$$             NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA)
    //$$     );
    //$$
    //$$     ModVoiceServer voiceServer = new ModVoiceServer(ModrinthLoader.FORGE);
    //$$     MinecraftForge.EVENT_BUS.register(voiceServer);
    //$$ }
    //#endif

    //#if FABRIC
    @Override
    public void onInitialize() {
        ModVoiceServer voiceServer = new ModVoiceServer(ModrinthLoader.FABRIC);
        voiceServer.onInitialize();
    }
    //#endif
}
