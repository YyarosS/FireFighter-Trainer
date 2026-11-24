using System;
using Firefighter.Application.Ports;
using UnityEngine;

namespace Firefighter.Infrastructure
{
    /// <summary>Журнал приложения поверх консоли Unity.</summary>
    public sealed class UnityLogSink : ILogSink
    {
        private const string Prefix = "[Firefighter] ";

        public void Info(string message)
        {
            Debug.Log(Prefix + message);
        }

        public void Warning(string message)
        {
            Debug.LogWarning(Prefix + message);
        }

        public void Error(string message, Exception exception = null)
        {
            Debug.LogError(exception == null ? Prefix + message : Prefix + message + Environment.NewLine + exception);
        }
    }
}