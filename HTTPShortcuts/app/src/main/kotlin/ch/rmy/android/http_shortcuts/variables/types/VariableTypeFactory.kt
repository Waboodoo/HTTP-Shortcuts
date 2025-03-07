package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.data.enums.VariableType as VariableTypeEnum
import javax.inject.Inject

class VariableTypeFactory
@Inject
constructor(
    private val constantType: ConstantType,
    private val textType: TextType,
    private val numberType: NumberType,
    private val passwordType: PasswordType,
    private val selectType: SelectType,
    private val colorType: ColorType,
    private val dateType: DateType,
    private val timeType: TimeType,
    private val sliderType: SliderType,
    private val toggleType: ToggleType,
    private val incrementType: IncrementType,
    private val uuidType: UUIDType,
    private val clipboardType: ClipboardType,
    private val timestampType: TimestampType,
) {

    fun getType(type: VariableTypeEnum): VariableType = when (type) {
        VariableTypeEnum.CONSTANT -> constantType
        VariableTypeEnum.TEXT -> textType
        VariableTypeEnum.NUMBER -> numberType
        VariableTypeEnum.PASSWORD -> passwordType
        VariableTypeEnum.SELECT -> selectType
        VariableTypeEnum.COLOR -> colorType
        VariableTypeEnum.DATE -> dateType
        VariableTypeEnum.TIME -> timeType
        VariableTypeEnum.SLIDER -> sliderType
        VariableTypeEnum.TOGGLE -> toggleType
        VariableTypeEnum.INCREMENT -> incrementType
        VariableTypeEnum.UUID -> uuidType
        VariableTypeEnum.CLIPBOARD -> clipboardType
        VariableTypeEnum.TIMESTAMP -> timestampType
    }
}
