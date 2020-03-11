package org.sagebionetworks.assessmentmodel.resourcemanagement

interface ResourceInfo {

    // MARK: Apple

    /**
     * The bundle that a given factory that was used to decode an object can use to load its resources. This is *always*
     * a pointer to the `Bundle` from which a JSON file was decoded but is defined generically here so that the lowest
     * level of the model does not include bundle information directly.
     */
    var decoderBundle: ResourceBundle?

    /**
     * The identifier of the bundle within which the resource is embedded on Apple platforms.
     */
    val bundleIdentifier: String?

    // MARK: Android

    /**
     * The package within which the resource is embedded on Android platforms.
     */
    var packageName: String?
}

/**
 * A resource bundle is used on Apple platforms to point to the `Bundle` for the resource. It is not directly
 * referenced within this framework so as to avoid any Apple-specific resource handling classes.
 */
interface ResourceBundle {
    val bundleIdentifier: String?
}

/**
 * The [AssetInfo] describes additional information for a *specific* resource. The [ResourceInfo] can be used to
 * describe a collection of resources within the same package or bundle whereas this is used to hold the
 * platform-specific information needed to load a single asset.
 *
 * @see: [AssetResourceInfo]
 */
interface AssetInfo {

    /**
     * The name of the resource.
     */
    val resourceName: String

    /**
     * For a raw resource file, this is the file extension for getting at the resource.
     */
    val rawFileExtension: String?

    // MARK: Android

    /**
     * The android-type of the resource.
     *
     * @see [StandardResourceAssetType] for the list of constants defining the asset type for the Android platform.
     *
     * @note: This is different from the Apple bundle structure where you would use either the raw file extension or
     * the initializer with the resource name and bundle to construct the object.
     */
    val resourceAssetType: String?
}

/**
 * An asset that defines its own resource info.
 */
interface AssetResourceInfo : AssetInfo, ResourceInfo

fun AssetResourceInfo.copyResourceInfo(fromResourceInfo: ResourceInfo) {
    this.decoderBundle = this.decoderBundle ?: fromResourceInfo.decoderBundle
    this.packageName = this.packageName ?: fromResourceInfo.packageName
}

object StandardResourceAssetType {
    const val DRAWABLE = "drawable"
    const val COLOR = "color"
    const val FONT = "font"
    const val RAW = "raw"
}
