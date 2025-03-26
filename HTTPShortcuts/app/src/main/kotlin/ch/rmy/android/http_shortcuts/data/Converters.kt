package ch.rmy.android.http_shortcuts.data

import android.net.Uri
import androidx.room.TypeConverter
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toCharset
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.SecurityPolicy
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import java.nio.charset.Charset
import java.time.Instant

class Converters {
    @TypeConverter
    fun deserializeInstant(value: Long?): Instant? =
        value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun serializeInstant(date: Instant?): Long? =
        date?.toEpochMilli()

    @TypeConverter
    fun deserializeHistoryEventType(value: String?): HistoryEventType? =
        value?.let { HistoryEventType.parse(it) }

    @TypeConverter
    fun serializeHistoryEventType(historyEventType: HistoryEventType?): String? =
        historyEventType?.type

    @TypeConverter
    fun deserializeUri(value: String?): Uri? =
        value?.let(Uri::parse)

    @TypeConverter
    fun serializeUri(uri: Uri?): String? =
        uri?.toString()

    @TypeConverter
    fun deserializeVariableType(value: String?): VariableType? =
        value?.let { VariableType.parse(it) }

    @TypeConverter
    fun serializeVariableType(variableType: VariableType): String =
        variableType.type

    @TypeConverter
    fun deserializeShortcutIcon(value: String?): ShortcutIcon =
        ShortcutIcon.fromName(value)

    @TypeConverter
    fun serializeShortcutIcon(shortcutIcon: ShortcutIcon?): String? =
        shortcutIcon?.toString()?.takeUnlessEmpty()

    @TypeConverter
    fun deserializeCategoryLayoutType(value: String?): CategoryLayoutType? =
        value?.let { CategoryLayoutType.parse(value) }

    @TypeConverter
    fun serializeCategoryLayoutType(categoryLayoutType: CategoryLayoutType?): String? =
        categoryLayoutType?.type

    @TypeConverter
    fun deserializeCategoryBackgroundType(value: String?): CategoryBackgroundType? =
        value?.let { CategoryBackgroundType.parse(value) }

    @TypeConverter
    fun serializeCategoryBackgroundType(categoryBackgroundType: CategoryBackgroundType?): String? =
        categoryBackgroundType?.serialize()

    @TypeConverter
    fun deserializeShortcutClickBehavior(value: String?): ShortcutClickBehavior? =
        value?.let { ShortcutClickBehavior.parse(value) }

    @TypeConverter
    fun serializeShortcutClickBehavior(shortcutClickBehavior: ShortcutClickBehavior?): String? =
        shortcutClickBehavior?.type

    @TypeConverter
    fun deserializeFileUploadType(value: String?): FileUploadType? =
        value?.let { FileUploadType.parse(value) }

    @TypeConverter
    fun serializeFileUploadType(fileUploadType: FileUploadType?): String? =
        fileUploadType?.type

    @TypeConverter
    fun deserializeShortcutExecutionType(value: String?): ShortcutExecutionType =
        value?.let { ShortcutExecutionType.parse(it) } ?: ShortcutExecutionType.HTTP

    @TypeConverter
    fun serializeShortcutExecutionType(shortcutExecutionType: ShortcutExecutionType?): String? =
        shortcutExecutionType?.type

    @TypeConverter
    fun deserializeSecurityPolicy(value: String?): SecurityPolicy? =
        value?.let { SecurityPolicy.parse(it) }

    @TypeConverter
    fun serializeSecurityPolicy(securityPolicy: SecurityPolicy): String =
        securityPolicy.serialize()

    @TypeConverter
    fun deserializeHttpMethod(value: String?): HttpMethod? =
        value?.let { HttpMethod.parse(it) }

    @TypeConverter
    fun serializeHttpMethod(httpMethod: HttpMethod): String =
        httpMethod.method

    @TypeConverter
    fun deserializeShortcutAuthenticationType(value: String?): ShortcutAuthenticationType? =
        value?.let { ShortcutAuthenticationType.parse(it) }

    @TypeConverter
    fun serializeShortcutAuthenticationType(shortcutAuthenticationType: ShortcutAuthenticationType): String =
        shortcutAuthenticationType.type

    @TypeConverter
    fun deserializeRequestBodyType(value: String?): RequestBodyType? =
        value?.let { RequestBodyType.parse(value) }

    @TypeConverter
    fun serializeRequestBodyType(requestBodyType: RequestBodyType): String =
        requestBodyType.type

    @TypeConverter
    fun deserializeParameterType(value: String?): ParameterType? =
        value?.let { ParameterType.parse(value) }

    @TypeConverter
    fun serializeParameterType(parameterType: ParameterType): String? =
        parameterType.type

    @TypeConverter
    fun deserializeResponseSuccessOutput(value: String?): ResponseSuccessOutput? =
        value?.let { ResponseSuccessOutput.parse(it) }

    @TypeConverter
    fun serializeResponseSuccessOutput(responseSuccessOutput: ResponseSuccessOutput): String =
        responseSuccessOutput.type

    @TypeConverter
    fun deserializeResponseFailureOutput(value: String?): ResponseFailureOutput? =
        value?.let { ResponseFailureOutput.parse(it) }

    @TypeConverter
    fun serializeResponseFailureOutput(responseFailureOutput: ResponseFailureOutput): String =
        responseFailureOutput.type

    @TypeConverter
    fun deserializeResponseUiType(value: String?): ResponseUiType? =
        value?.let { ResponseUiType.parse(value) }

    @TypeConverter
    fun serializeResponseUiType(responseUiType: ResponseUiType): String =
        responseUiType.type

    @TypeConverter
    fun deserializeResponseContentType(value: String?): ResponseContentType? =
        value?.let { ResponseContentType.parse(value) }

    @TypeConverter
    fun deserializeTargetBrowser(value: String?): TargetBrowser? =
        value?.let { TargetBrowser.parse(value) }

    @TypeConverter
    fun serializeTargetBrowser(targetBrowser: TargetBrowser): String? =
        targetBrowser.serialize()

    @TypeConverter
    fun deserializeClientCertParams(value: String?): ClientCertParams? =
        value?.let { ClientCertParams.parse(value) }

    @TypeConverter
    fun serializeClientCertParams(clientCertParams: ClientCertParams): String =
        clientCertParams.toString()

    @TypeConverter
    fun deserializeIpVersion(value: String?): IpVersion? =
        value?.let { IpVersion.parse(value) }

    @TypeConverter
    fun serializeIpVersion(ipVersion: IpVersion): String =
        ipVersion.version

    @TypeConverter
    fun deserializeProxyType(value: String?): ProxyType? =
        value?.let { ProxyType.parse(value) }

    @TypeConverter
    fun serializeProxyType(proxyType: ProxyType): String =
        proxyType.type

    @TypeConverter
    fun deserializeConfirmationType(value: String?): ConfirmationType? =
        value?.let { ConfirmationType.parse(value) }

    @TypeConverter
    fun serializeConfirmationType(confirmationType: ConfirmationType): String =
        confirmationType.type

    @TypeConverter
    fun deserializeCharset(value: String?): Charset? =
        value?.toCharset()

    @TypeConverter
    fun serializeCharset(charset: Charset): String =
        charset.name()
}
