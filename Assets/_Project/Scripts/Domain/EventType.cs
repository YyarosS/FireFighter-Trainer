namespace Firefighter.Domain
{
    /// <summary>
    /// Тип события телеметрии. Значения совпадают с контрактом сервера
    /// (EventType на Java), строковое представление формируется в
    /// инфраструктурном слое при сериализации.
    /// </summary>
    public enum EventType
    {
        /// <summary>Корректное действие стажёра.</summary>
        Action = 0,

        /// <summary>Нарушение техники безопасности.</summary>
        SafetyViolation = 1,

        /// <summary>Использовано неверное оборудование.</summary>
        WrongEquipment = 2,

        /// <summary>Пропущен обязательный этап упражнения.</summary>
        MissedStage = 3
    }
}