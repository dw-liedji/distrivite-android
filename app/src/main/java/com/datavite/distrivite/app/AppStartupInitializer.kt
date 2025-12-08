package com.datavite.distrivite.app
import kotlinx.coroutines.flow.debounce
import android.content.Context
import android.util.Log
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.data.sync.PushSyncWorker
import com.datavite.distrivite.data.sync.SyncMetadataManager
import com.datavite.distrivite.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Singleton
class AppStartupInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val syncMetadataManager: SyncMetadataManager,
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    @param:ApplicationScope private val scope: CoroutineScope,
) {

    @OptIn(FlowPreview::class)
    fun setupWork() {

        scope.launch(Dispatchers.IO) {
            authOrgUserCredentialManager.sharedAuthOrgUserFlow.collectLatest { authOrgUser ->
               authOrgUser?.let {
                   syncMetadataManager.ensureInitialized()
                   Log.i("AppStartupInitializer", "AppStartupInitializer enqueue SyncOrchestratorWorker Periodic $it")
                   PushSyncWorker.MultiSyncScheduler.schedulePeriodicSync(context, it.orgSlug, 30)
               }
            }
        }

        scope.launch(Dispatchers.IO) {
            networkStatusMonitor.isConnected
                .debounce(500)
                .distinctUntilChanged()
                .collect { isConnected ->
                    if (isConnected) {
                        authOrgUserCredentialManager.sharedAuthOrgUserFlow.firstOrNull()?.orgSlug?.let { orgSlug ->
                            Log.i("AppStartupInitializer", "AppStartupInitializer network available one time enqueue SyncOrchestratorWorker Now $orgSlug")
                            PushSyncWorker.MultiSyncScheduler.enqueueNow(context, orgSlug)
                            //PushAndPullSyncWorker.MultiSyncScheduler.enqueueNow(context, orgSlug)
                        }
                    }
                }
        }
    }
}
