package com.motormeet.uruguay.util

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

val CartoVoyagerTileSource = object : OnlineTileSourceBase(
    "CartoVoyager",
    0, 20, 256, ".png",
    arrayOf("https://basemaps.cartocdn.com/rastertiles/voyager/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val z = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$baseUrl$z/$x/$y.png?key=cb1_4xsu_1_ac0923699c5ceb9ea51e1048"
    }
}

val CartoDarkTileSource = object : OnlineTileSourceBase(
    "CartoDBDark",
    0, 20, 256, ".png",
    arrayOf("https://a.basemaps.cartocdn.com/dark_all/", "https://b.basemaps.cartocdn.com/dark_all/", "https://c.basemaps.cartocdn.com/dark_all/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }
}
