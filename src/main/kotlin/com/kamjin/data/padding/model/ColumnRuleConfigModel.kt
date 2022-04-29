package com.kamjin.data.padding.model

import tornadofx.*

/**
 * <p>
 *
 * </p>
 *
 * @author kam
 * @since 2022/04/28
 */

/**
 * 列规则配置
 */
class ColumnRuleConfigModel : ItemViewModel<ColumnMetadata>() {

    val columnName = bind(ColumnMetadata::nameProperty)

    val tableName = bind(ColumnMetadata::tableNameProperty)

    val columnType = bind(ColumnMetadata::typeProperty)

    val columnLength = bind(ColumnMetadata::lengthProperty)

    val columnComment = bind(ColumnMetadata::commentProperty)

    val selectedRule = bind(ColumnMetadata::selectRuleProperty)

    val ruleFunName = bind(ColumnMetadata::ruleFunNameProperty)

    val ruleFun = bind(ColumnMetadata::ruleFunProperty)

    val ruleFunParam = bind(ColumnMetadata::ruleFunParamProperty)

    val otherTableColumnKey = bind(ColumnMetadata::otherTableColumnKeyProperty)

    val customScriptFilters = bind(ColumnMetadata::customScriptFiltersProperty)

}