package com.datavite.distrivite.app

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.data.sync.PushSyncWorker
import com.datavite.distrivite.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


@Singleton
class AppLifecycleObserver @Inject constructor (
    @param:ApplicationContext private val context: Context,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    @param:ApplicationScope private val scope: CoroutineScope,
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        // App moved to foreground
        Log.i("MyApplication", "App in foreground")
        // Trigger sync fetch/push here
    }

    override fun onStop(owner: LifecycleOwner) {
        // App moved to background
        Log.i("MyApplication", "App in background")
        // Pause sync or cleanup here
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.i("MyApplication", "App in onResume")

        scope.launch {
            val isConnected = networkStatusMonitor.isConnected.first()
            val authUser = authOrgUserCredentialManager.sharedAuthOrgUserFlow.firstOrNull()

            if (isConnected && authUser != null) {
                Log.i("MyApplication", "App Work started in onResume")
                PushSyncWorker.MultiSyncScheduler.enqueueNow(context, authUser.orgSlug)
            }
        }
    }
}