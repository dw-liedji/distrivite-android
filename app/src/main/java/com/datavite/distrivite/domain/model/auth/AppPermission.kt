package com.datavite.distrivite.domain.model.auth

enum class AppPermission(val code: String) {

    // ---- Account ----
    ACCOUNT_ADD_EMAILADDRESS("account.add_emailaddress"),
    ACCOUNT_CHANGE_EMAILADDRESS("account.change_emailaddress"),
    ACCOUNT_DELETE_EMAILADDRESS("account.delete_emailaddress"),
    ACCOUNT_VIEW_EMAILADDRESS("account.view_emailaddress"),
    ACCOUNT_ADD_EMAILCONFIRMATION("account.add_emailconfirmation"),
    ACCOUNT_CHANGE_EMAILCONFIRMATION("account.change_emailconfirmation"),
    ACCOUNT_DELETE_EMAILCONFIRMATION("account.delete_emailconfirmation"),
    ACCOUNT_VIEW_EMAILCONFIRMATION("account.view_emailconfirmation"),

    // ---- Admin ----
    ADMIN_ADD_LOGENTRY("admin.add_logentry"),
    ADMIN_CHANGE_LOGENTRY("admin.change_logentry"),
    ADMIN_DELETE_LOGENTRY("admin.delete_logentry"),
    ADMIN_VIEW_LOGENTRY("admin.view_logentry"),

    // ---- Auth ----
    AUTH_ADD_GROUP("auth.add_group"),
    AUTH_CHANGE_GROUP("auth.change_group"),
    AUTH_DELETE_GROUP("auth.delete_group"),
    AUTH_VIEW_GROUP("auth.view_group"),
    AUTH_ADD_PERMISSION("auth.add_permission"),
    AUTH_CHANGE_PERMISSION("auth.change_permission"),
    AUTH_DELETE_PERMISSION("auth.delete_permission"),
    AUTH_VIEW_PERMISSION("auth.view_permission"),

    // ---- Core ----
    CORE_ADD_FAQ("core.add_faq"),
    CORE_CHANGE_FAQ("core.change_faq"),
    CORE_DELETE_FAQ("core.delete_faq"),
    CORE_VIEW_FAQ("core.view_faq"),

    // ---- Orders ----
    ORDERS_ADD_BATCH("orders.add_batch"),
    ORDERS_CHANGE_BATCH("orders.change_batch"),
    ORDERS_DELETE_BATCH("orders.delete_batch"),
    ORDERS_VIEW_BATCH("orders.view_batch"),

    ORDERS_ADD_CATEGORY("orders.add_category"),
    ORDERS_CHANGE_CATEGORY("orders.change_category"),
    ORDERS_DELETE_CATEGORY("orders.delete_category"),
    ORDERS_VIEW_CATEGORY("orders.view_category"),

    ORDERS_ADD_ITEM("orders.add_item"),
    ORDERS_CHANGE_ITEM("orders.change_item"),
    ORDERS_DELETE_ITEM("orders.delete_item"),
    ORDERS_VIEW_ITEM("orders.view_item"),

    ORDERS_ADD_STOCK("orders.add_stock"),
    ORDERS_CHANGE_STOCK("orders.change_stock"),
    ORDERS_CHANGE_STOCK_PRICE("orders.change_stockprice"),
    ORDERS_DELETE_STOCK("orders.delete_stock"),
    ORDERS_VIEW_STOCK("orders.view_stock"),

    ORDERS_ADD_BILLING("orders.add_facturation"),
    ORDERS_CHANGE_BILLING("orders.change_facturation"),
    ORDERS_DELETE_BILLING("orders.delete_facturation"),
    ORDERS_DELIVER_BILLING("orders.deliver_facturation"),
    ORDERS_PRINT_BILLING("orders.print_facturation"),
    ORDERS_VIEW_BILLING("orders.view_facturation"),


    ORDERS_ADD_BILLING_STOCK("orders.add_facturationstock"),
    ORDERS_CHANGE_BILLING_STOCK("orders.change_facturationstock"),
    ORDERS_DELETE_BILLING_STOCK("orders.delete_facturationstock"),
    ORDERS_DELIVER_BILLING_STOCK("orders.deliver_facturationstock"),
    ORDERS_PRINT_BILLING_STOCK("orders.print_facturationstock"),
    ORDERS_VIEW_BILLING_STOCK("orders.view_facturationstock"),



    ORDERS_ADD_BILLING_PAYMENT("orders.add_facturationpayment"),
    ORDERS_CHANGE_BILLING_PAYMENT("orders.change_facturationpayment"),
    ORDERS_DELETE_BILLING_PAYMENT("orders.delete_facturationpayment"),
    ORDERS_PRINT_BILLING_PAYMENT("orders.print_facturationpayment"),
    ORDERS_VIEW_BILLING_PAYMENT("orders.view_facturationpayment"),

    ORDERS_ADD_TRANSACTION("orders.add_transaction"),
    ORDERS_CHANGE_TRANSACTION("orders.change_transaction"),
    ORDERS_DELETE_TRANSACTION("orders.delete_transaction"),
    ORDERS_PRINT_TRANSACTION("orders.print_transaction"),
    ORDERS_VIEW_TRANSACTION("orders.view_transaction"),

    // ---- Organization ----
    ORGANIZATION_ADD("organization.add_organization"),
    ORGANIZATION_CHANGE("organization.change_organization"),
    ORGANIZATION_DELETE("organization.delete_organization"),
    ORGANIZATION_VIEW("organization.view_organization"),
    ORGANIZATION_VIEW_DASHBOARD("organization.view_organizationdashboard"),

    // ---- Sessions ----
    SESSIONS_ADD("sessions.add_session"),
    SESSIONS_CHANGE("sessions.change_session"),
    SESSIONS_DELETE("sessions.delete_session"),
    SESSIONS_VIEW("sessions.view_session"),

    // ---- Users ----
    USERS_ADD("users.add_user"),
    USERS_CHANGE("users.change_user"),
    USERS_DELETE("users.delete_user"),
    USERS_VIEW("users.view_user");

    companion object {
        private val map = entries.associateBy { it.code }

        fun from(code: String): AppPermission? = map[code]
    }
}

// Extension for checking permissions
fun Set<String>.has(permission: AppPermission):
        Boolean = permission.code in this