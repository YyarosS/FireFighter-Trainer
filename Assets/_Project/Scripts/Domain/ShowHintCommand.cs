using System;

namespace Firefighter.Domain
{
    /// <summary>Подсказка обучаемому, решение о которой принимает сервер.</summary>
    public sealed class ShowHintCommand : ServerCommand
    {
        public ShowHintCommand(long sequence, DateTimeOffset issuedAt, string text)
            : base(sequence, issuedAt)
        {
            if (string.IsNullOrWhiteSpace(text))
            {
                throw new ArgumentException("Текст подсказки не может быть пустым", nameof(text));
            }

            Text = text;
        }

        public string Text { get; }

        public override string Type => "SHOW_HINT";

        public override void ApplyTo(TrainingSessionState state)
        {
            state.AddHint(Text);
        }
    }
}