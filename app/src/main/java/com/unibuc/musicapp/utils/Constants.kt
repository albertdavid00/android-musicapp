package com.unibuc.musicapp.utils

object Constants {
    const val BASE_URL = "http://192.168.0.103:8080"; // 192.168.0.103 - ipconfig in cmd
    const val ACCESS_TOKEN = "access_token"
    const val AZURE_BLOB_SAS_URL = "https://unibucmusicappstorage.blob.core.windows.net/musicblobdata/"
    const val AZURE_BLOB_SAS_TOKEN = "?sp=rcw&st=2024-03-17T21:15:34Z&se=2025-01-01T05:15:34Z&spr=https&sv=2022-11-02&sr=c&sig=D4GmApLMpu%2FnqhAl2MpUd3YGkfmds9wEQZnTAB6FA5c%3D"
    const val AZURE_CDN_PREFIX_URL = "https://musicappcdn.azureedge.net/musicblobdata/"
    const val AZURE_QUEUE_SAS_URL = "https://unibucmusicappstorage.queue.core.windows.net/musicapp-post-queue/messages?sv=2022-11-02&ss=q&srt=sco&sp=rwdlacup&se=2025-01-01T05:46:37Z&st=2024-03-18T21:46:37Z&spr=https&sig=syXFSeYrL1NwKYOfOvQAicI7L7ct3QiMSSVQ%2B5JPjLE%3D"
}