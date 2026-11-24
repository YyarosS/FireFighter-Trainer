using System;

namespace Firefighter.Application.Ports
{
    /// <summary>
    /// Порт журналирования.
    ///
    /// <para>Прикладной слой не вызывает <c>Debug.Log</c> напрямую:
    /// в редакторе сообщения идут в консоль Unity, в сборке — в файл
    /// или систему телеметрии.</para>
    /// </summary>
    public interface ILogSink
    {
        void Info(string message);

        void Warning(string message);

        void Error(string message, Exception exception = null);
    }
}