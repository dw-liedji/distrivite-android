package com.datavite.distrivite.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.datavite.distrivite.data.local.dao.ClaimDao
import com.datavite.distrivite.data.local.dao.EmployeeDao
import com.datavite.distrivite.data.local.dao.HolidayDao
import com.datavite.distrivite.data.local.dao.LeaveDao
import com.datavite.distrivite.data.local.dao.LocalBillingDao
import com.datavite.distrivite.data.local.dao.LocalBillingItemDao
import com.datavite.distrivite.data.local.dao.LocalBulkCreditPaymentDao
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
import com.datavite.distrivite.data.local.model.LocalBilling
import com.datavite.distrivite.data.local.model.LocalBillingItem
import com.datavite.distrivite.data.local.model.LocalBillingPayment
import com.datavite.distrivite.data.local.model.LocalBulkCreditPayment
import com.datavite.distrivite.data.local.model.LocalClaim
import com.datavite.distrivite.data.local.model.LocalCustomer
import com.datavite.distrivite.data.local.model.LocalEmployee
import com.datavite.distrivite.data.local.model.LocalHoliday
import com.datavite.distrivite.data.local.model.LocalInstructorContract
import com.datavite.distrivite.data.local.model.LocalLeave
import com.datavite.distrivite.data.local.model.LocalOrganizationUser
import com.datavite.distrivite.data.local.model.LocalRoom
import com.datavite.distrivite.data.local.model.LocalStock
import com.datavite.distrivite.data.local.model.LocalStudent
import com.datavite.distrivite.data.local.model.LocalStudentAttendance
import com.datavite.distrivite.data.local.model.LocalTeachingCourse
import com.datavite.distrivite.data.local.model.LocalTeachingPeriod
import com.datavite.distrivite.data.local.model.LocalTeachingSession
import com.datavite.distrivite.data.local.model.LocalTransaction
import com.datavite.distrivite.data.local.model.LocalWorkingPeriod
import com.datavite.distrivite.data.local.model.PendingNotificationAction
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncMetadata

@Database(entities = [
    LocalOrganizationUser::class,
    SyncMetadata::class,
    PendingOperation::class,
    PendingNotificationAction::class,
    LocalEmployee::class,
    LocalStudent::class,
    LocalInstructorContract::class,
    LocalTeachingCourse::class,
    LocalTeachingSession::class,
    LocalWorkingPeriod::class,
    LocalStudentAttendance::class,
    LocalLeave::class,
    LocalHoliday::class,
    LocalClaim::class,
    LocalTeachingPeriod::class,
    LocalRoom::class,
    LocalCustomer::class,
    LocalStock::class,
    LocalBilling::class,
    LocalBillingItem::class,
    LocalBillingPayment::class,
    LocalTransaction::class,
    LocalBulkCreditPayment::class,
     ],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    Converters::class,
    DatabaseConverters::class,
    NotificationStatusConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingNotificationDao(): PendingNotificationDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun organizationUserDao(): OrganizationUserDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun localStudentDao(): LocalStudentDao
    abstract fun localTeachingCourseDao(): LocalTeachingCourseDao
    abstract fun localTeachingSessionDao(): LocalTeachingSessionDao
    abstract fun localInstructorContractDao(): LocalInstructorContractDao
    abstract fun leaveDao(): LeaveDao
    abstract fun holidayDao(): HolidayDao
    abstract fun claimDao(): ClaimDao
    abstract fun roomDao(): RoomDao
    abstract fun teachingPeriodDao(): TeachingPeriodDao
    abstract fun workingPeriodDao(): WorkingPeriodDao
    abstract fun studentAttendanceDao(): StudentAttendanceDao
    abstract fun localStockDao(): LocalStockDao
    abstract fun localCustomerDao(): LocalCustomerDao
    abstract fun localTransactionDao(): LocalTransactionDao
    abstract fun localBulkCreditPaymentDao(): LocalBulkCreditPaymentDao
    abstract fun localBillingDao(): LocalBillingDao
    abstract fun localBillingItemDao(): LocalBillingItemDao
    abstract fun syncMetadataDao(): SyncMetadataDao  // Add this
}