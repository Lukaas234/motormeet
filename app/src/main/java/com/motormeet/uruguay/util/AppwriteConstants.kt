package com.motormeet.uruguay.util

import io.appwrite.Client

object AppwriteConstants {
    const val APPWRITE_PUBLIC_ENDPOINT = "https://nyc.cloud.appwrite.io/v1"
    const val APPWRITE_PROJECT_ID = "6a74de1c003b6edf4fe4"
    const val DATABASE_ID = "6a750db7001ae758ecbe"
    const val USERS_COLLECTION_ID = "users"
    const val POSTS_COLLECTION_ID = "posts"
    const val EVENTS_COLLECTION_ID = "events"
    const val VEHICLES_COLLECTION_ID = "vehicles"
    const val COMMENTS_COLLECTION_ID = "comments"
    const val NOTIFICATIONS_COLLECTION_ID = "notifications"
    const val CHANNELS_COLLECTION_ID = "channels"
    const val CHANNEL_MESSAGES_COLLECTION_ID = "channel_messages"
    const val STORAGE_BUCKET_ID = "6a751906000225a1ebc2"
}

suspend fun Client.ping(): Any {
    return this.call(
        method = "GET",
        path = "/ping",
        responseType = Map::class.java
    )
}
