package su.plo.voice.client.crowdin

import com.google.common.collect.ImmutableSet
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import java.io.File
import java.io.InputStream

//#if MC>=11903
import net.minecraft.server.packs.resources.IoSupplier

//#if MC>=12005
//$$ import net.minecraft.server.packs.PackLocationInfo
//#endif

//#else
//$$ import java.io.FileInputStream
//$$ import java.util.function.Predicate
//#endif

class PlasmoCrowdinPack(
    private val crowdinFolder: File
) : PackResources {

    override fun close() {}

    //#if MC>=11903
    override fun getRootResource(vararg fileNames: String): IoSupplier<InputStream>? =
        File(crowdinFolder, fileNames[0])
            .takeIf { it.exists() }
            ?.let { IoSupplier.create(it.toPath()) }

    override fun getResource(packType: PackType, resourceLocation: ResourceLocation): IoSupplier<InputStream>? {
        if (resourceLocation.namespace != "plasmovoice") return null
        if (!resourceLocation.path.startsWith("lang/")) return null
        return getRootResource(resourceLocation.path.substringAfter("lang/"))
    }

    override fun listResources(
        packType: PackType,
        namespace: String,
        prefix: String,
        resourceOutput: PackResources.ResourceOutput
    ) {}

    //#if MC>=12005
    //$$ override fun location(): net.minecraft.server.packs.PackLocationInfo {
    //$$     throw UnsupportedOperationException()
    //$$ }
    //#else
    override fun isBuiltin() = true
    //#endif

    //#else
    //$$ override fun getRootResource(fileName: String): InputStream? =
    //$$     File(crowdinFolder, fileName)
    //$$         .takeIf { it.exists() }
    //$$         ?.let { FileInputStream(it) }
    //$$
    //$$ override fun getResource(packType: PackType, resourceLocation: ResourceLocation): InputStream =
    //$$     getRootResource(resourceLocation.path.substringAfter("lang/"))!!
    //$$
    //#if MC>=11900
    //$$ override fun getResources(
    //$$     packType: PackType,
    //$$     namespace: String,
    //$$     prefix: String,
    //$$     predicate: Predicate<ResourceLocation>
    //$$ ): Collection<ResourceLocation> = emptyList()
    //#else
    //$$ override fun getResources(
    //$$     packType: PackType,
    //$$     string: String,
    //$$     string2: String,
    //$$     i: Int,
    //$$     predicate: Predicate<String>
    //$$ ): Collection<ResourceLocation> = emptyList()
    //#endif
    //$$
    //$$ override fun hasResource(packType: PackType, resourceLocation: ResourceLocation): Boolean {
    //$$     if (resourceLocation.namespace != "plasmovoice") return false
    //$$     if (!resourceLocation.path.startsWith("lang/")) return false
    //$$     return File(crowdinFolder, resourceLocation.path.substringAfter("lang/")).exists()
    //$$ }
    //#endif

    override fun getNamespaces(packType: PackType): Set<String> = NAMESPACES

    override fun <T : Any?> getMetadataSection(metadataSectionSerializer: MetadataSectionSerializer<T>) = null

    //#if MC>11902
    override fun packId() = "Plasmo Crowdin resource pack"
    //#else
    //$$ override fun getName() = "Plasmo Crowdin resource pack"
    //#endif

    companion object {

        private val NAMESPACE = "plasmovoice"
        private val NAMESPACES: Set<String> = ImmutableSet.of(NAMESPACE)
    }
}
