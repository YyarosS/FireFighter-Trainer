using System;

namespace Firefighter.Domain
{
    /// <summary>Команда изменения условий среды по решению сервера.</summary>
    public sealed class UpdateEnvironmentCommand : ServerCommand
    {
        public UpdateEnvironmentCommand(long sequence, DateTimeOffset issuedAt, EnvironmentSnapshot environment)
            : base(sequence, issuedAt)
        {
            Environment = environment ?? throw new ArgumentNullException(nameof(environment));
        }

        public EnvironmentSnapshot Environment { get; }

        public override string Type => "UPDATE_ENVIRONMENT";

        public override void ApplyTo(TrainingSessionState state)
        {
            state.SetEnvironmentTarget(Environment);
        }
    }
}