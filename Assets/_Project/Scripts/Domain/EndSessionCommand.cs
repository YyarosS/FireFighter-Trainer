using System;

namespace Firefighter.Domain
{
    /// <summary>Итоговая команда: сервер сообщает рассчитанные рейтинги.</summary>
    public sealed class EndSessionCommand : ServerCommand
    {
        public EndSessionCommand(
            long sequence,
            DateTimeOffset issuedAt,
            double speed,
            double accuracy,
            double combined)
            : base(sequence, issuedAt)
        {
            Speed = speed;
            Accuracy = accuracy;
            Combined = combined;
        }

        public double Speed { get; }

        public double Accuracy { get; }

        public double Combined { get; }

        public override string Type => "END_SESSION";

        public override void ApplyTo(TrainingSessionState state)
        {
            state.Complete(new TrainingResult(Speed, Accuracy, Combined, string.Empty));
        }
    }
}