package ru.yandex.autotests.oebs.tools.lib.common;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by dmtdlm on 23.11.2016.
 */
public abstract class QueueProcessor<T>
{
    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    //private RequestProcessorThread processorThread = null;
    private ProcessorThread[] threads;
    //List<ProcessorThread> threads;
    private String threadName;
    private int threadNumber = 0;
    private int maxThreadCount = 0;

    public QueueProcessor(String threadName, int maxThreadCount)
    {
        this.threadName = threadName;
        this.maxThreadCount = maxThreadCount;
        //noinspection unchecked
        threads = new QueueProcessor.ProcessorThread[maxThreadCount];
        for (int i = 0; i < maxThreadCount; i++)
            threads[i] = null;
    }

    public void clearQueue()
    {
        queue.clear();
    }

    public void addTask(T task)
    {
        synchronized (queue)
        {
            if (!queue.contains(task))
                queue.add(task);
            startThread();
        }
    }

    private boolean threadAlive(ProcessorThread thread)
    {
        return thread != null && thread.isAlive() && thread.isRunning();
    }

    private void startThread()
    {
        synchronized (threads)
        {
            //определяем число живых потоков
            int aliveCount = 0;
            for (ProcessorThread thread : threads)
                if (threadAlive(thread))
                    aliveCount++;

            //бежим по всем потокам и воскрешаем до тех пор,
            //пока их число не будет соответствовать очереди или достигнет предела
            int n = 0;
            for (int i = aliveCount; i < Math.min(maxThreadCount, queue.size()); i++)
            {
                for (int j = n; j < maxThreadCount; j++)
                {
                    if (!threadAlive(threads[j]))
                    {
                        threads[j] = new ProcessorThread();
                        threads[j].start();
                        n = j + 1;
                        break;
                    }
                }
            }
        }
    }

    protected abstract void processTask(T task);

    //Класс потока обработчика
    private class ProcessorThread extends Thread
    {
        private boolean running = true;

        public ProcessorThread()
        {
            setName(threadName + (threadNumber++));
        }

        public void run()
        {
            while (running)
            {
                T task = null;
                synchronized (queue)
                {
                    task = queue.poll();
                    if (task == null)
                    {
                        running = false;
                        break;
                    }
                }
                processTask(task);
            }
        }

        public boolean isRunning()
        {
            return running;
        }
    }


}
