package com.datavite.distrivite.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.datavite.distrivite.feature.cameis.domain.repository.RoomRepository
import com.datavite.distrivite.BuildConfig
import com.datavite.distrivite.app.AppLifecycleObserver
import com.datavite.distrivite.app.AppStartupInitializer
import com.datavite.distrivite.data.local.database.AppDatabase
import com.datavite.distrivite.data.local.dao.ClaimDao
import com.datavite.distrivite.data.local.dao.EmployeeDao
import com.datavite.distrivite.data.local.dao.HolidayDao
import com.datavite.distrivite.data.local.dao.LeaveDao
import com.datavite.distrivite.data.local.dao.LocalBillingDao
import com.datavite.distrivite.data.local.dao.LocalCustomerDao
import com.datavite.distrivite.data.local.dao.LocalInstructorContractDao
import com.datavite.distrivite.data.local.dao.LocalStockDao
import com.datavite.distrivite.data.local.dao.LocalStudentDao
import com.datavite.distrivite.data.local.dao.LocalTeachingCourseDao
import com.datavite.distrivite.data.local.dao.LocalTeachingSessionDao
import com.datavite.distrivite.data.local.dao.LocalTransactionDao
import com.datavite.distrivite.data.local.dao.OrganizationUserDao
import com.datavite.distrivite.data.local.dao.PendingNotificationDao
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.dao.RoomDao
import com.datavite.distrivite.data.local.dao.StudentAttendanceDao
import com.datavite.distrivite.data.local.dao.SyncMetadataDao
import com.datavite.distrivite.data.local.dao.TeachingPeriodDao
import com.datavite.distrivite.data.local.dao.WorkingPeriodDao
import com.datavite.distrivite.data.local.datasource.BillingLocalDataSource
import com.datavite.distrivite.data.local.datasource.BillingLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.ClaimLocalDataSource
import com.datavite.distrivite.data.local.datasource.ClaimLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.CustomerLocalDataSource
import com.datavite.distrivite.data.local.datasource.CustomerLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.EmployeeLocalDataSource
import com.datavite.distrivite.data.local.datasource.EmployeeLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.HolidayLocalDataSource
import com.datavite.distrivite.data.local.datasource.HolidayLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.InstructorContractLocalDataSource
import com.datavite.distrivite.data.local.datasource.InstructorContractLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.LeaveLocalDataSource
import com.datavite.distrivite.data.local.datasource.LeaveLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.OrganizationUserLocalDataSource
import com.datavite.distrivite.data.local.datasource.OrganizationUserLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.RoomLocalDataSource
import com.datavite.distrivite.data.local.datasource.RoomLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.StockLocalDataSource
import com.datavite.distrivite.data.local.datasource.StockLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.StudentAttendanceLocalDataSource
import com.datavite.distrivite.data.local.datasource.StudentAttendanceLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.StudentLocalDataSource
import com.datavite.distrivite.data.local.datasource.StudentLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.TeachingCourseLocalDataSource
import com.datavite.distrivite.data.local.datasource.TeachingCourseLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.TeachingPeriodLocalDataSource
import com.datavite.distrivite.data.local.datasource.TeachingPeriodLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.TeachingSessionLocalDataSource
import com.datavite.distrivite.data.local.datasource.TeachingSessionLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.TransactionLocalDataSource
import com.datavite.distrivite.data.local.datasource.TransactionLocalDataSourceImpl
import com.datavite.distrivite.data.local.datasource.WorkingPeriodLocalDataSource
import com.datavite.distrivite.data.local.datasource.WorkingPeriodLocalDataSourceImpl
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.local.datastore.UserCredentialManager
import com.datavite.distrivite.data.location.geofence.GeofenceLocationDataStore
import com.datavite.distrivite.data.location.geofence.GeofenceRepository
import com.datavite.distrivite.data.mapper.BillingItemMapper
import com.datavite.distrivite.data.mapper.BillingMapper
import com.datavite.distrivite.data.mapper.BillingPaymentMapper
import com.datavite.distrivite.data.mapper.ClaimMapper
import com.datavite.distrivite.data.mapper.CustomerMapper
import com.datavite.distrivite.data.mapper.EmployeeMapper
import com.datavite.distrivite.data.mapper.HolidayMapper
import com.datavite.distrivite.data.mapper.InstructorContractMapper
import com.datavite.distrivite.data.mapper.LeaveMapper
import com.datavite.distrivite.data.mapper.OrganizationUserMapper
import com.datavite.distrivite.data.mapper.RoomMapper
import com.datavite.distrivite.data.mapper.StockMapper
import com.datavite.distrivite.data.mapper.StudentAttendanceMapper
import com.datavite.distrivite.data.mapper.StudentMapper
import com.datavite.distrivite.data.mapper.TeachingCourseMapper
import com.datavite.distrivite.data.mapper.TeachingPeriodMapper
import com.datavite.distrivite.data.mapper.TeachingSessionMapper
import com.datavite.distrivite.data.mapper.TransactionMapper
import com.datavite.distrivite.data.mapper.WorkingPeriodMapper
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.data.notification.NotificationBusImpl
import com.datavite.distrivite.data.notification.NotificationOrchestrator
import com.datavite.distrivite.data.notification.NotificationService
import com.datavite.distrivite.data.notification.TextToSpeechNotifier
import com.datavite.distrivite.data.notification.services.NotificationApiGateway
import com.datavite.distrivite.data.notification.services.ParentNotificationService
import com.datavite.distrivite.data.notification.services.ParentNotificationServiceImpl
import com.datavite.distrivite.data.remote.clients.PublicRetrofitClient
import com.datavite.distrivite.data.remote.datasource.BillingRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.BillingRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.ClaimRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.ClaimRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.CustomerRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.CustomerRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.EmployeeRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.EmployeeRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.HolidayRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.HolidayRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.InstructorContractRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.InstructorContractRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.LeaveRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.LeaveRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.OrganizationUserRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.OrganizationUserRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.RoomRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.RoomRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.StockRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.StockRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.StudentAttendanceRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.StudentAttendanceRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.StudentRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.StudentRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.TeachingCourseRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.TeachingCourseRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.TeachingPeriodRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.TeachingPeriodRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.TeachingSessionRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.TeachingSessionRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.TransactionRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.TransactionRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.datasource.WorkingPeriodRemoteDataSource
import com.datavite.distrivite.data.remote.datasource.WorkingPeriodRemoteDataSourceImpl
import com.datavite.distrivite.data.remote.service.ClaimService
import com.datavite.distrivite.data.remote.service.EmployeeService
import com.datavite.distrivite.data.remote.service.HolidayService
import com.datavite.distrivite.data.remote.service.LeaveService
import com.datavite.distrivite.data.remote.service.OrganizationUserService
import com.datavite.distrivite.data.remote.service.RemoteBillingService
import com.datavite.distrivite.data.remote.service.RemoteCustomerService
import com.datavite.distrivite.data.remote.service.RemoteInstructorContractService
import com.datavite.distrivite.data.remote.service.RemoteStockService
import com.datavite.distrivite.data.remote.service.RemoteStudentService
import com.datavite.distrivite.data.remote.service.RemoteTeachingCourseService
import com.datavite.distrivite.data.remote.service.RemoteTeachingSessionService
import com.datavite.distrivite.data.remote.service.RemoteTransactionService
import com.datavite.distrivite.data.remote.service.RoomService
import com.datavite.distrivite.data.remote.service.StudentAttendanceService
import com.datavite.distrivite.data.remote.service.TeachingPeriodService
import com.datavite.distrivite.data.remote.service.WorkingPeriodService
import com.datavite.distrivite.data.repository.BillingRepositoryImpl
import com.datavite.distrivite.data.repository.ClaimRepositoryImpl
import com.datavite.distrivite.data.repository.CustomerRepositoryImpl
import com.datavite.distrivite.data.repository.EmployeeRepositoryImpl
import com.datavite.distrivite.data.repository.HolidayRepositoryImpl
import com.datavite.distrivite.data.repository.InstructorContractRepositoryImpl
import com.datavite.distrivite.data.repository.LeaveRepositoryImpl
import com.datavite.distrivite.data.repository.OrganizationUserRepositoryImpl
import com.datavite.distrivite.data.repository.RoomRepositoryImpl
import com.datavite.distrivite.data.repository.StockRepositoryImpl
import com.datavite.distrivite.data.repository.StudentAttendanceRepositoryImpl
import com.datavite.distrivite.data.repository.StudentRepositoryImpl
import com.datavite.distrivite.data.repository.TeachingCourseRepositoryImpl
import com.datavite.distrivite.data.repository.TeachingPeriodRepositoryImpl
import com.datavite.distrivite.data.repository.TeachingSessionRepositoryImpl
import com.datavite.distrivite.data.repository.TransactionRepositoryImpl
import com.datavite.distrivite.data.repository.WorkingPeriodRepositoryImpl
import com.datavite.distrivite.data.speech.SpeechRecognitionManager
import com.datavite.distrivite.data.sync.SyncMetadataManager
import com.datavite.distrivite.data.sync.SyncOrchestrator
import com.datavite.distrivite.data.sync.SyncService
import com.datavite.distrivite.data.sync.services.BillingSyncService
import com.datavite.distrivite.data.sync.services.BillingSyncServiceImpl
import com.datavite.distrivite.data.sync.services.CustomerSyncService
import com.datavite.distrivite.data.sync.services.CustomerSyncServiceImpl
import com.datavite.distrivite.data.sync.services.StockSyncService
import com.datavite.distrivite.data.sync.services.StockSyncServiceImpl
import com.datavite.distrivite.data.sync.services.StudentAttendanceSyncService
import com.datavite.distrivite.data.sync.services.StudentAttendanceSyncServiceImpl
import com.datavite.distrivite.data.sync.services.TeachingSessionSyncService
import com.datavite.distrivite.data.sync.services.TeachingSessionSyncServiceImpl
import com.datavite.distrivite.data.sync.services.TransactionSyncService
import com.datavite.distrivite.data.sync.services.TransactionSyncServiceImpl
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.repository.BillingRepository
import com.datavite.distrivite.domain.repository.ClaimRepository
import com.datavite.distrivite.domain.repository.CustomerRepository
import com.datavite.distrivite.domain.repository.EmployeeRepository
import com.datavite.distrivite.domain.repository.HolidayRepository
import com.datavite.distrivite.domain.repository.InstructorContractRepository
import com.datavite.distrivite.domain.repository.LeaveRepository
import com.datavite.distrivite.domain.repository.OrganizationUserRepository
import com.datavite.distrivite.domain.repository.StockRepository
import com.datavite.distrivite.domain.repository.StudentAttendanceRepository
import com.datavite.distrivite.domain.repository.StudentRepository
import com.datavite.distrivite.domain.repository.TeachingCourseRepository
import com.datavite.distrivite.domain.repository.TeachingPeriodRepository
import com.datavite.distrivite.domain.repository.TeachingSessionRepository
import com.datavite.distrivite.domain.repository.TransactionRepository
import com.datavite.distrivite.domain.repository.WorkingPeriodRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TiqtaqModule {

    @Provides
    @Singleton
    fun provideGeofenceLocationDataStore(@ApplicationContext context: Context) : GeofenceLocationDataStore {
        return GeofenceLocationDataStore(context)
    }

    @Provides
    @Singleton
    fun provideGeofenceRepository(@ApplicationContext context: Context): GeofenceRepository {
        return GeofenceRepository(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNetworkStatusMonitor(@ApplicationContext context: Context): NetworkStatusMonitor {
        return NetworkStatusMonitor(context)
    }

    @Provides
    fun provideOrganizationUserDao(database: AppDatabase): OrganizationUserDao {
        return database.organizationUserDao()
    }

    @Provides
    fun provideEmployeeDao(database: AppDatabase): EmployeeDao {
        return database.employeeDao()
    }

    @Provides
    fun provideWorkingPeriodDao(database: AppDatabase): WorkingPeriodDao {
        return database.workingPeriodDao()
    }



    @Provides
    @Singleton
    fun provideSpeechRecognitionManager(@ApplicationContext context: Context): SpeechRecognitionManager {
        return SpeechRecognitionManager(context)
    }


    @Provides
    @Singleton
    fun provideRetrofitWorkingPeriodService(publicRetrofitClient: PublicRetrofitClient): WorkingPeriodService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(WorkingPeriodService::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofitStudentAttendanceService(publicRetrofitClient: PublicRetrofitClient): StudentAttendanceService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(StudentAttendanceService::class.java)
    }


    @Provides
    @Singleton
    fun provideWorkingPeriodLocalDataSource(
        workingPeriodDao: WorkingPeriodDao
    ): WorkingPeriodLocalDataSource {
        return WorkingPeriodLocalDataSourceImpl(workingPeriodDao)
    }

    @Provides
    @Singleton
    fun provideWorkingPeriodRemoteDataSource(
        workingPeriodService: WorkingPeriodService
    ): WorkingPeriodRemoteDataSource {
        return WorkingPeriodRemoteDataSourceImpl(workingPeriodService)
    }

    @Provides
    @Singleton
    fun provideWorkingPeriodMapper(): WorkingPeriodMapper {
        return WorkingPeriodMapper()
    }

    @Provides
    @Singleton
    fun provideWorkingPeriodRepository(
        localDataSource: WorkingPeriodLocalDataSource,
        remoteDataSource: WorkingPeriodRemoteDataSource,
        workingPeriodMapper: WorkingPeriodMapper

    ): WorkingPeriodRepository {
        return WorkingPeriodRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            workingPeriodMapper = workingPeriodMapper
        )
    }

    @Provides
    @Singleton
    fun provideOrganizationUserCredentialManager(dataStore: DataStore<Preferences>): UserCredentialManager {
        return UserCredentialManager(dataStore=dataStore)
    }

    @Provides
    @Singleton
    fun provideRetrofitOrganizationUserService(publicRetrofitClient: PublicRetrofitClient): OrganizationUserService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(OrganizationUserService::class.java)
    }

    @Provides
    @Singleton
    fun provideOrganizationUserLocalDataSource(
        organizationUserDao: OrganizationUserDao
    ): OrganizationUserLocalDataSource {
        return OrganizationUserLocalDataSourceImpl(organizationUserDao)
    }

    @Provides
    @Singleton
    fun provideOrganizationUserRemoteDataSource(
        organizationUserService: OrganizationUserService
    ): OrganizationUserRemoteDataSource {
        return OrganizationUserRemoteDataSourceImpl(organizationUserService)
    }

    @Provides
    @Singleton
    fun provideOrganizationUserMapper(): OrganizationUserMapper {
        return OrganizationUserMapper()
    }



    @Provides
    @Singleton
    fun provideOrganizationUserRepository(
        localOrganizationUserLocalDataSource: OrganizationUserLocalDataSource,
        remoteOrganizationUserRemoteDataSource: OrganizationUserRemoteDataSource,
        organizationUserMapper: OrganizationUserMapper
    ): OrganizationUserRepository {
        return OrganizationUserRepositoryImpl(
            localDataSource = localOrganizationUserLocalDataSource,
            remoteDataSource = remoteOrganizationUserRemoteDataSource,
            organizationUserMapper = organizationUserMapper
        )
    }

    @Provides
    fun provideStudentAttendanceDao(database: AppDatabase): StudentAttendanceDao {
        return database.studentAttendanceDao()
    }

    @Provides
    @Singleton
    fun provideRetrofitEmployeeService(publicRetrofitClient: PublicRetrofitClient): EmployeeService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(EmployeeService::class.java)
    }

    @Provides
    @Singleton
    fun provideEmployeeLocalDataSource(
        employeeDao: EmployeeDao
    ): EmployeeLocalDataSource {
        return EmployeeLocalDataSourceImpl(employeeDao)
    }

    @Provides
    @Singleton
    fun provideEmployeeRemoteDataSource(
        employeeService: EmployeeService
    ): EmployeeRemoteDataSource {
        return EmployeeRemoteDataSourceImpl(employeeService)
    }

    @Provides
    @Singleton
    fun provideEmployeeMapper(): EmployeeMapper {
        return EmployeeMapper()
    }

    @Provides
    @Singleton
    fun provideEmployeeRepository(
        localEmployeeLocalDataSource: EmployeeLocalDataSource,
        remoteEmployeeRemoteDataSource: EmployeeRemoteDataSource,
        employeeMapper: EmployeeMapper
    ): EmployeeRepository {
        return EmployeeRepositoryImpl(
            localDataSource = localEmployeeLocalDataSource,
            remoteDataSource = remoteEmployeeRemoteDataSource,
            employeeMapper = employeeMapper
        )
    }

    @Provides
    @Singleton
    fun provideStudentAttendanceLocalDataSource(
        employeeAttendanceDao: StudentAttendanceDao
    ): StudentAttendanceLocalDataSource {
        return StudentAttendanceLocalDataSourceImpl(employeeAttendanceDao)
    }

    @Provides
    @Singleton
    fun provideStudentAttendanceRemoteDataSource(
        employeeAttendance: StudentAttendanceService
    ): StudentAttendanceRemoteDataSource {
        return StudentAttendanceRemoteDataSourceImpl(employeeAttendance)
    }

    @Provides
    @Singleton
    fun provideStudentAttendanceMapper(): StudentAttendanceMapper {
        return StudentAttendanceMapper()
    }
    @Provides
    @Singleton
    fun provideStudentAttendanceRepository(
        localDataSource: StudentAttendanceLocalDataSource,
        remoteDataSource: StudentAttendanceRemoteDataSource,
        studentAttendanceMapper: StudentAttendanceMapper,
        pendingOperationDao: PendingOperationDao
    ): StudentAttendanceRepository {
        return StudentAttendanceRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            studentAttendanceMapper = studentAttendanceMapper,
            pendingOperationDao = pendingOperationDao
        )
    }


    @Provides
    @Singleton
    fun provideStudentAttendanceSyncService(
        localDataSource: StudentAttendanceLocalDataSource,
        remoteDataSource: StudentAttendanceRemoteDataSource,
        studentAttendanceMapper: StudentAttendanceMapper,
        pendingOperationDao: PendingOperationDao
    ): StudentAttendanceSyncService {
        return StudentAttendanceSyncServiceImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            studentAttendanceMapper = studentAttendanceMapper,
            pendingOperationDao = pendingOperationDao
        )
    }

    @Provides
    fun provideLeaveDao(database: AppDatabase): LeaveDao {
        return database.leaveDao()
    }

    @Provides
    fun providePendingOperationDao(database: AppDatabase): PendingOperationDao {
        return database.pendingOperationDao()
    }

    @Provides
    fun providePendingNotificationDao(database: AppDatabase): PendingNotificationDao {
        return database.pendingNotificationDao()
    }

    @Provides
    @Singleton
    fun provideRetrofitLeaveService(publicRetrofitClient: PublicRetrofitClient): LeaveService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(LeaveService::class.java)
    }

    @Provides
    @Singleton
    fun provideLeaveLocalDataSource(
        leaveDao: LeaveDao
    ): LeaveLocalDataSource {
        return LeaveLocalDataSourceImpl(leaveDao)
    }

    @Provides
    @Singleton
    fun provideLeaveRemoteDataSource(
        leaveService: LeaveService
    ): LeaveRemoteDataSource {
        return LeaveRemoteDataSourceImpl(leaveService)
    }

    @Provides
    @Singleton
    fun provideLeaveMapper(): LeaveMapper {
        return LeaveMapper()
    }

    @Provides
    @Singleton
    fun provideLeaveRepository(
        localLeaveLocalDataSource: LeaveLocalDataSource,
        remoteLeaveRemoteDataSource: LeaveRemoteDataSource,
        leaveMapper: LeaveMapper
    ): LeaveRepository {
        return LeaveRepositoryImpl(
            localDataSource = localLeaveLocalDataSource,
            remoteDataSource = remoteLeaveRemoteDataSource,
            leaveMapper = leaveMapper
        )
    }


    @Provides
    fun provideHolidayDao(database: AppDatabase): HolidayDao {
        return database.holidayDao()
    }

    @Provides
    @Singleton
    fun provideRetrofitHolidayService(publicRetrofitClient: PublicRetrofitClient): HolidayService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(HolidayService::class.java)
    }

    @Provides
    @Singleton
    fun provideHolidayLocalDataSource(
        holidayDao: HolidayDao
    ): HolidayLocalDataSource {
        return HolidayLocalDataSourceImpl(holidayDao)
    }

    @Provides
    @Singleton
    fun provideHolidayRemoteDataSource(
        holidayService: HolidayService
    ): HolidayRemoteDataSource {
        return HolidayRemoteDataSourceImpl(holidayService)
    }

    @Provides
    @Singleton
    fun provideHolidayMapper(): HolidayMapper {
        return HolidayMapper()
    }

    @Provides
    @Singleton
    fun provideHolidayRepository(
        localHolidayLocalDataSource: HolidayLocalDataSource,
        remoteHolidayRemoteDataSource: HolidayRemoteDataSource,
        holidayMapper: HolidayMapper
    ): HolidayRepository {
        return HolidayRepositoryImpl(
            localDataSource = localHolidayLocalDataSource,
            remoteDataSource = remoteHolidayRemoteDataSource,
            holidayMapper = holidayMapper
        )
    }

    @Provides
    fun provideClaimDao(database: AppDatabase): ClaimDao {
        return database.claimDao()
    }

    @Provides
    @Singleton
    fun provideRetrofitClaimService(publicRetrofitClient: PublicRetrofitClient): ClaimService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(ClaimService::class.java)
    }

    @Provides
    @Singleton
    fun provideClaimLocalDataSource(
        claimDao: ClaimDao
    ): ClaimLocalDataSource {
        return ClaimLocalDataSourceImpl(claimDao)
    }

    @Provides
    @Singleton
    fun provideClaimRemoteDataSource(
        claimService: ClaimService
    ): ClaimRemoteDataSource {
        return ClaimRemoteDataSourceImpl(claimService)
    }

    @Provides
    @Singleton
    fun provideClaimMapper(): ClaimMapper {
        return ClaimMapper()
    }

    @Provides
    @Singleton
    fun provideClaimRepository(
        localClaimLocalDataSource: ClaimLocalDataSource,
        remoteClaimRemoteDataSource: ClaimRemoteDataSource,
        claimMapper: ClaimMapper
    ): ClaimRepository {
        return ClaimRepositoryImpl(
            localDataSource = localClaimLocalDataSource,
            remoteDataSource = remoteClaimRemoteDataSource,
            claimMapper = claimMapper
        )
    }



    @Provides
    fun provideLocalStudentDao(database: AppDatabase): LocalStudentDao {
        return database.localStudentDao()
    }

    @Provides
    @Singleton
    fun provideRemoteStudentService(publicRetrofitClient: PublicRetrofitClient): RemoteStudentService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteStudentService::class.java)
    }

    @Provides
    @Singleton
    fun provideStudentLocalDataSource(
        localStudentDao: LocalStudentDao
    ): StudentLocalDataSource {
        return StudentLocalDataSourceImpl(localStudentDao)
    }

    @Provides
    @Singleton
    fun provideStudentRemoteDataSource(
        remoteStudentService: RemoteStudentService
    ): StudentRemoteDataSource {
        return StudentRemoteDataSourceImpl(remoteStudentService)
    }

    @Provides
    @Singleton
    fun provideStudentMapper(): StudentMapper {
        return StudentMapper()
    }

    @Provides
    @Singleton
    fun provideStudentRepository(
        localStudentLocalDataSource: StudentLocalDataSource,
        remoteStudentRemoteDataSource: StudentRemoteDataSource,
        studentMapper: StudentMapper
    ): StudentRepository {
        return StudentRepositoryImpl(
            localDataSource = localStudentLocalDataSource,
            remoteDataSource = remoteStudentRemoteDataSource,
            studentMapper = studentMapper
        )
    }


    @Provides
    fun provideLocalInstructorContractDao(database: AppDatabase): LocalInstructorContractDao {
        return database.localInstructorContractDao()
    }

    @Provides
    @Singleton
    fun provideRemoteInstructorContractService(publicRetrofitClient: PublicRetrofitClient): RemoteInstructorContractService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteInstructorContractService::class.java)
    }

    @Provides
    @Singleton
    fun provideInstructorContractLocalDataSource(
        localInstructorContractDao: LocalInstructorContractDao
    ): InstructorContractLocalDataSource {
        return InstructorContractLocalDataSourceImpl(localInstructorContractDao)
    }

    @Provides
    @Singleton
    fun provideInstructorContractRemoteDataSource(
        remoteInstructorContractService: RemoteInstructorContractService
    ): InstructorContractRemoteDataSource {
        return InstructorContractRemoteDataSourceImpl(remoteInstructorContractService)
    }

    @Provides
    @Singleton
    fun provideInstructorContractMapper(): InstructorContractMapper {
        return InstructorContractMapper()
    }

    @Provides
    @Singleton
    fun provideInstructorContractRepository(
        localInstructorContractLocalDataSource: InstructorContractLocalDataSource,
        remoteInstructorContractRemoteDataSource: InstructorContractRemoteDataSource,
        instructorContractMapper: InstructorContractMapper
    ): InstructorContractRepository {
        return InstructorContractRepositoryImpl(
            localDataSource = localInstructorContractLocalDataSource,
            remoteDataSource = remoteInstructorContractRemoteDataSource,
            instructorContractMapper = instructorContractMapper
        )
    }


    @Provides
    fun provideLocalTeachingCourseDao(database: AppDatabase): LocalTeachingCourseDao {
        return database.localTeachingCourseDao()
    }

    @Provides
    @Singleton
    fun provideRemoteTeachingCourseService(publicRetrofitClient: PublicRetrofitClient): RemoteTeachingCourseService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteTeachingCourseService::class.java)
    }

    @Provides
    @Singleton
    fun provideTeachingCourseLocalDataSource(
        localTeachingCourseDao: LocalTeachingCourseDao
    ): TeachingCourseLocalDataSource {
        return TeachingCourseLocalDataSourceImpl(localTeachingCourseDao)
    }

    @Provides
    @Singleton
    fun provideTeachingCourseRemoteDataSource(
        remoteTeachingCourseService: RemoteTeachingCourseService
    ): TeachingCourseRemoteDataSource {
        return TeachingCourseRemoteDataSourceImpl(remoteTeachingCourseService)
    }

    @Provides
    @Singleton
    fun provideTeachingCourseMapper(): TeachingCourseMapper {
        return TeachingCourseMapper()
    }

    @Provides
    @Singleton
    fun provideTeachingCourseRepository(
        localTeachingCourseLocalDataSource: TeachingCourseLocalDataSource,
        remoteTeachingCourseRemoteDataSource: TeachingCourseRemoteDataSource,
        teachingCourseMapper: TeachingCourseMapper
    ): TeachingCourseRepository {
        return TeachingCourseRepositoryImpl(
            localDataSource = localTeachingCourseLocalDataSource,
            remoteDataSource = remoteTeachingCourseRemoteDataSource,
            teachingCourseMapper = teachingCourseMapper
        )
    }


    @Provides
    fun provideLocalTeachingSessionDao(database: AppDatabase): LocalTeachingSessionDao {
        return database.localTeachingSessionDao()
    }

    @Provides
    @Singleton
    fun provideRemoteTeachingSessionService(publicRetrofitClient: PublicRetrofitClient): RemoteTeachingSessionService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteTeachingSessionService::class.java)
    }

    @Provides
    @Singleton
    fun provideTeachingSessionLocalDataSource(
        localTeachingSessionDao: LocalTeachingSessionDao
    ): TeachingSessionLocalDataSource {
        return TeachingSessionLocalDataSourceImpl(localTeachingSessionDao)
    }

    @Provides
    @Singleton
    fun provideTeachingSessionRemoteDataSource(
        remoteTeachingSessionService: RemoteTeachingSessionService
    ): TeachingSessionRemoteDataSource {
        return TeachingSessionRemoteDataSourceImpl(remoteTeachingSessionService)
    }

    @Provides
    @Singleton
    fun provideTeachingSessionMapper(): TeachingSessionMapper {
        return TeachingSessionMapper()
    }

    @Provides
    @Singleton
    fun provideTeachingSessionRepository(
        remoteDataSource: TeachingSessionRemoteDataSource,
        localDataSource: TeachingSessionLocalDataSource,
        teachingSessionMapper: TeachingSessionMapper,
        pendingOperationDao: PendingOperationDao,
        pendingNotificationDao: PendingNotificationDao,
        notificationBus: NotificationBus
    ): TeachingSessionRepository {
        return TeachingSessionRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            teachingSessionMapper = teachingSessionMapper,
            pendingOperationDao = pendingOperationDao,
            pendingNotificationDao = pendingNotificationDao,
            notificationBus = notificationBus
        )
    }

    @Provides
    @Singleton
    fun provideTeachingSessionSyncService(
        remoteDataSource: TeachingSessionRemoteDataSource,
        localDataSource: TeachingSessionLocalDataSource,
        teachingSessionMapper: TeachingSessionMapper,
        pendingOperationDao: PendingOperationDao,
    ): TeachingSessionSyncService {
        return TeachingSessionSyncServiceImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            teachingSessionMapper = teachingSessionMapper,
            pendingOperationDao = pendingOperationDao,
        )
    }


    @Provides
    @Singleton
    fun provideSyncOrchestrator(
        pendingOperationDao: PendingOperationDao,
        syncServices: Array<SyncService>,  // <-- Inject Array here
        networkManager: NetworkStatusMonitor
    ): SyncOrchestrator {
        return SyncOrchestrator(
            pendingOperationDao = pendingOperationDao,
            syncServices = syncServices,
        )
    }


    @Provides
    @Singleton
    fun provideRetrofitNotificationApiGateway(publicRetrofitClient: PublicRetrofitClient): NotificationApiGateway {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(NotificationApiGateway::class.java)
    }

    @Provides
    @Singleton
    fun provideParentNotificationService(
        teachingSessionRepository: TeachingSessionRepository,
        studentAttendanceRepository: StudentAttendanceRepository,
        pendingNotificationDao: PendingNotificationDao,
        notificationApiGateway: NotificationApiGateway,
        teachingSessionMapper: TeachingSessionMapper,
        notificationBus: NotificationBus
    ): ParentNotificationService {
        return ParentNotificationServiceImpl(
            teachingSessionRepository = teachingSessionRepository,
            studentAttendanceRepository = studentAttendanceRepository,
            pendingNotificationDao = pendingNotificationDao,
            notificationApiGateway = notificationApiGateway,
            teachingSessionMapper = teachingSessionMapper,
            notificationBus = notificationBus
        )
    }

    @Provides
    @Singleton
    fun provideNotificationBus(): NotificationBus = NotificationBusImpl()

    @Provides
    @Singleton
    fun provideNotificationOrchestrator(
        notificationServices: Array<NotificationService>,  // <-- Inject Array here
    ): NotificationOrchestrator {
        return NotificationOrchestrator(
            notificationServices = notificationServices
        )
    }



    @Provides
    @Singleton
    fun provideSyncServices(
        stockSyncService: StockSyncService,
        customerSyncService: CustomerSyncService,
        transactionSyncService: TransactionSyncService,
        billingSyncService: BillingSyncService,
        teachingSessionSyncService: TeachingSessionSyncService,
        studentAttendanceSyncService: StudentAttendanceSyncService
        // other sync services
    ): Array<SyncService> {
        return arrayOf(
            customerSyncService, // first in syncing order
            stockSyncService, // second in syncing order
            billingSyncService, // third in syncing order
            transactionSyncService, // fourth in syncing order
            //teachingSessionSyncService,  // five in syncing order
            //studentAttendanceSyncService // sixth in syncing order
        )
    }


    @Provides
    @Singleton
    fun provideNotificationServices(
        parentNotificationService: ParentNotificationService,
        // other sync services
    ): Array<NotificationService> {
        return arrayOf(
            parentNotificationService,  // first in syncing order
        )
    }

    @Provides
    @Singleton
    fun provideText2SpeechNotifier(
        @ApplicationContext context: Context
    ): TextToSpeechNotifier {
        return TextToSpeechNotifier(context)
    }

    @Provides
    @Singleton
    fun provideAppStartupInitializer(
        @ApplicationContext context: Context,
        networkStatusMonitor: NetworkStatusMonitor,
        syncMetadataManager: SyncMetadataManager,
        authOrgUserCredentialManager: AuthOrgUserCredentialManager,
        @ApplicationScope scope: CoroutineScope,
    ) : AppStartupInitializer {
        return AppStartupInitializer(
            context=context,
            networkStatusMonitor=networkStatusMonitor,
            syncMetadataManager = syncMetadataManager,
            authOrgUserCredentialManager,
            scope=scope)
    }

    @Provides
    @Singleton
    fun provideAppLifecycleObserver(
        @ApplicationContext context: Context,
        networkStatusMonitor: NetworkStatusMonitor,
        authOrgUserCredentialManager: AuthOrgUserCredentialManager,
        @ApplicationScope scope: CoroutineScope,
    ) : AppLifecycleObserver {
        return AppLifecycleObserver(
            context=context,
            networkStatusMonitor=networkStatusMonitor,
            authOrgUserCredentialManager,
            scope=scope)
    }
    @Provides
    fun provideRoomDao(database: AppDatabase): RoomDao {
        return database.roomDao()
    }

    @Provides
    @Singleton
    fun provideRetrofitRoomService(publicRetrofitClient: PublicRetrofitClient): RoomService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RoomService::class.java)
    }

    @Provides
    @Singleton
    fun provideRoomLocalDataSource(
        roomDao: RoomDao
    ): RoomLocalDataSource {
        return RoomLocalDataSourceImpl(roomDao)
    }

    @Provides
    @Singleton
    fun provideRoomRemoteDataSource(
        roomService: RoomService
    ): RoomRemoteDataSource {
        return RoomRemoteDataSourceImpl(roomService)
    }

    @Provides
    @Singleton
    fun provideRoomMapper(): RoomMapper {
        return RoomMapper()
    }

    @Provides
    @Singleton
    fun provideRoomRepository(
        roomLocalDataSource: RoomLocalDataSource,
        roomRemoteDataSource: RoomRemoteDataSource,
        roomMapper: RoomMapper
    ): RoomRepository {
        return RoomRepositoryImpl(
            roomLocalDataSource = roomLocalDataSource,
            roomRemoteDataSource = roomRemoteDataSource,
            roomMapper = roomMapper
        )
    }



    @Provides
    fun provideTeachingPeriodDao(database: AppDatabase): TeachingPeriodDao {
        return database.teachingPeriodDao()
    }

    @Provides
    @Singleton
    fun provideRetrofitTeachingPeriodService(publicRetrofitClient: PublicRetrofitClient): TeachingPeriodService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(TeachingPeriodService::class.java)
    }

    @Provides
    @Singleton
    fun provideTeachingPeriodLocalDataSource(
        teachingPeriodDao: TeachingPeriodDao
    ): TeachingPeriodLocalDataSource {
        return TeachingPeriodLocalDataSourceImpl(teachingPeriodDao)
    }

    @Provides
    @Singleton
    fun provideTeachingPeriodRemoteDataSource(
        teachingPeriodService: TeachingPeriodService
    ): TeachingPeriodRemoteDataSource {
        return TeachingPeriodRemoteDataSourceImpl(teachingPeriodService)
    }

    @Provides
    @Singleton
    fun provideTeachingPeriodMapper(): TeachingPeriodMapper {
        return TeachingPeriodMapper()
    }

    @Provides
    @Singleton
    fun provideTeachingPeriodRepository(
        teachingPeriodLocalDataSource: TeachingPeriodLocalDataSource,
        teachingPeriodRemoteDataSource: TeachingPeriodRemoteDataSource,
        teachingPeriodMapper: TeachingPeriodMapper
    ): TeachingPeriodRepository {
        return TeachingPeriodRepositoryImpl(
            teachingPeriodLocalDataSource = teachingPeriodLocalDataSource,
            teachingPeriodRemoteDataSource = teachingPeriodRemoteDataSource,
            teachingPeriodMapper = teachingPeriodMapper
        )
    }

    @Provides
    fun provideLocalStockDao(database: AppDatabase): LocalStockDao {
        return database.localStockDao()
    }

    @Provides
    @Singleton
    fun provideRemoteStockService(publicRetrofitClient: PublicRetrofitClient): RemoteStockService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteStockService::class.java)
    }

    @Provides
    @Singleton
    fun provideStockLocalDataSource(
        localStockDao: LocalStockDao
    ): StockLocalDataSource {
        return StockLocalDataSourceImpl(localStockDao)
    }

    @Provides
    @Singleton
    fun provideStockRemoteDataSource(
        remoteStockService: RemoteStockService
    ): StockRemoteDataSource {
        return StockRemoteDataSourceImpl(remoteStockService)
    }

    @Provides
    @Singleton
    fun provideStockMapper(): StockMapper {
        return StockMapper()
    }

    @Provides
    @Singleton
    fun provideStockRepository(
        remoteDataSource: StockRemoteDataSource,
        localDataSource: StockLocalDataSource,
        stockMapper: StockMapper,
        pendingOperationDao: PendingOperationDao,
        pendingNotificationDao: PendingNotificationDao,
        notificationBus: NotificationBus
    ): StockRepository {
        return StockRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            stockMapper = stockMapper,
            pendingOperationDao = pendingOperationDao,
            pendingNotificationDao = pendingNotificationDao,
            notificationBus = notificationBus
        )
    }

    @Provides
    @Singleton
    fun provideStockSyncService(
        remoteDataSource: StockRemoteDataSource,
        localDataSource: StockLocalDataSource,
        stockMapper: StockMapper,
        syncMetadataDao: SyncMetadataDao,
        pendingOperationDao: PendingOperationDao,
    ): StockSyncService {
        return StockSyncServiceImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            stockMapper = stockMapper,
            syncMetadataDao = syncMetadataDao,
            pendingOperationDao = pendingOperationDao,
        )
    }




    @Provides
    fun provideLocalBillingDao(database: AppDatabase): LocalBillingDao {
        return database.localBillingDao()
    }

    @Provides
    @Singleton
    fun provideRemoteBillingService(publicRetrofitClient: PublicRetrofitClient): RemoteBillingService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteBillingService::class.java)
    }

    @Provides
    @Singleton
    fun provideBillingLocalDataSource(
        localBillingDao: LocalBillingDao
    ): BillingLocalDataSource {
        return BillingLocalDataSourceImpl(localBillingDao)
    }

    @Provides
    @Singleton
    fun provideBillingRemoteDataSource(
        remoteBillingService: RemoteBillingService
    ): BillingRemoteDataSource {
        return BillingRemoteDataSourceImpl(remoteBillingService)
    }

    @Provides
    @Singleton
    fun provideBillingItemMapper(): BillingItemMapper {
        return BillingItemMapper()
    }

    @Provides
    @Singleton
    fun provideBillingPaymentMapper(): BillingPaymentMapper {
        return BillingPaymentMapper()
    }

    @Provides
    @Singleton
    fun provideBillingMapper(
        billingItemMapper: BillingItemMapper,
        billingPaymentMapper: BillingPaymentMapper
    ): BillingMapper {
        return BillingMapper(billingItemMapper=billingItemMapper, billingPaymentMapper = billingPaymentMapper)
    }

    @Provides
    @Singleton
    fun provideBillingRepository(
        remoteDataSource: BillingRemoteDataSource,
        localDataSource: BillingLocalDataSource,
        billingMapper: BillingMapper,
        pendingOperationDao: PendingOperationDao,
        pendingNotificationDao: PendingNotificationDao,
        notificationBus: NotificationBus
    ): BillingRepository {
        return BillingRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            billingMapper = billingMapper,
            pendingOperationDao = pendingOperationDao,
            pendingNotificationDao = pendingNotificationDao,
            notificationBus = notificationBus
        )
    }

    @Provides
    @Singleton
    fun provideSyncMetadataDao(database: AppDatabase): SyncMetadataDao {
        return database.syncMetadataDao()
    }
    @Provides
    @Singleton
    fun provideSyncMetadataManager(
        syncMetadataDao: SyncMetadataDao
    ): SyncMetadataManager {
        return SyncMetadataManager(syncMetadataDao)
    }

    @Provides
    @Singleton
    fun provideBillingSyncService(
        remoteDataSource: BillingRemoteDataSource,
        localDataSource: BillingLocalDataSource,
        billingMapper: BillingMapper,
        syncMetadataDao: SyncMetadataDao,
        pendingOperationDao: PendingOperationDao,
    ): BillingSyncService {
        return BillingSyncServiceImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            billingMapper = billingMapper,
            syncMetadataDao = syncMetadataDao,
            pendingOperationDao = pendingOperationDao,
        )
    }


    @Provides
    fun provideLocalTransactionDao(database: AppDatabase): LocalTransactionDao {
        return database.localTransactionDao()
    }

    @Provides
    @Singleton
    fun provideRemoteTransactionService(publicRetrofitClient: PublicRetrofitClient): RemoteTransactionService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteTransactionService::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionLocalDataSource(
        localTransactionDao: LocalTransactionDao
    ): TransactionLocalDataSource {
        return TransactionLocalDataSourceImpl(localTransactionDao)
    }

    @Provides
    @Singleton
    fun provideTransactionRemoteDataSource(
        remoteTransactionService: RemoteTransactionService,
    ): TransactionRemoteDataSource {
        return TransactionRemoteDataSourceImpl(remoteTransactionService)
    }

    @Provides
    @Singleton
    fun provideTransactionMapper(): TransactionMapper {
        return TransactionMapper()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        remoteDataSource: TransactionRemoteDataSource,
        localDataSource: TransactionLocalDataSource,
        transactionMapper: TransactionMapper,
        pendingOperationDao: PendingOperationDao,
        pendingNotificationDao: PendingNotificationDao,
        notificationBus: NotificationBus
    ): TransactionRepository {
        return TransactionRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            transactionMapper = transactionMapper,
            pendingOperationDao = pendingOperationDao,
            pendingNotificationDao = pendingNotificationDao,
            notificationBus = notificationBus
        )
    }

    @Provides
    @Singleton
    fun provideTransactionSyncService(
        remoteDataSource: TransactionRemoteDataSource,
        localDataSource: TransactionLocalDataSource,
        transactionMapper: TransactionMapper,
        syncMetadataDao: SyncMetadataDao,
        pendingOperationDao: PendingOperationDao,
    ): TransactionSyncService {
        return TransactionSyncServiceImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            transactionMapper = transactionMapper,
            syncMetadataDao = syncMetadataDao,
            pendingOperationDao = pendingOperationDao,
        )
    }

    @Provides
    fun provideLocalCustomerDao(database: AppDatabase): LocalCustomerDao {
        return database.localCustomerDao()
    }

    @Provides
    @Singleton
    fun provideRemoteCustomerService(publicRetrofitClient: PublicRetrofitClient): RemoteCustomerService {
        return publicRetrofitClient.getRetrofit(BuildConfig.BASE_URL)
            .create(RemoteCustomerService::class.java)
    }

    @Provides
    @Singleton
    fun provideCustomerLocalDataSource(
        localCustomerDao: LocalCustomerDao
    ): CustomerLocalDataSource {
        return CustomerLocalDataSourceImpl(localCustomerDao)
    }

    @Provides
    @Singleton
    fun provideCustomerRemoteDataSource(
        remoteCustomerService: RemoteCustomerService
    ): CustomerRemoteDataSource {
        return CustomerRemoteDataSourceImpl(remoteCustomerService)
    }

    @Provides
    @Singleton
    fun provideCustomerMapper(): CustomerMapper {
        return CustomerMapper()
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(
        remoteDataSource: CustomerRemoteDataSource,
        localDataSource: CustomerLocalDataSource,
        customerMapper: CustomerMapper,
        pendingOperationDao: PendingOperationDao,
        pendingNotificationDao: PendingNotificationDao,
        notificationBus: NotificationBus
    ): CustomerRepository {
        return CustomerRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            customerMapper = customerMapper,
            pendingOperationDao = pendingOperationDao,
            pendingNotificationDao = pendingNotificationDao,
            notificationBus = notificationBus
        )
    }

    @Provides
    @Singleton
    fun provideCustomerSyncService(
        remoteDataSource: CustomerRemoteDataSource,
        localDataSource: CustomerLocalDataSource,
        customerMapper: CustomerMapper,
        syncMetadataDao: SyncMetadataDao,
        pendingOperationDao: PendingOperationDao,
    ): CustomerSyncService {
        return CustomerSyncServiceImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            customerMapper = customerMapper,
            syncMetadataDao = syncMetadataDao,
            pendingOperationDao = pendingOperationDao,
        )
    }

}