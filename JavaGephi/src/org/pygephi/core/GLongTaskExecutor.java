package org.pygephi.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class GLongTaskExecutor {

    private final boolean inBackground;
    private boolean interruptCancel;
    private final long interruptDelay;
    private final String name;
    private RunningLongTask runningTask;
    private Timer cancelTimer;
    private ExecutorService executor;

    /**
     * Creates a new long task executor.
     *
     * @param doInBackground when <code>true</code>, the task will be executed
     * in a separate thread
     * @param name the name of the executor, used to recognize threads by names
     * @param interruptDelay number of seconds to wait before *
     * calling <code>Thread.interrupt()</code> after a cancel request
     */
    public GLongTaskExecutor(boolean doInBackground, String name, float interruptDelay) {
        this.inBackground = doInBackground;
        this.name = name;
        this.interruptCancel = true;
        this.interruptDelay = (int) interruptDelay * 1000;
    }
    
    public String getName(){
    	return this.name;
    }

    /**
     * Creates a new long task executor.
     *
     * @param doInBackground doInBackground when <code>true</code>, the task
     * will be executed in a separate thread
     * @param name the name of the executor, used to recognize threads by names
     */
    public GLongTaskExecutor(boolean doInBackground, String name) {
        this(doInBackground, name, 0);
        this.interruptCancel = false;
    }

    /**
     * Creates a new long task executor.
     *
     * @param doInBackground doInBackground when <code>true</code>, the task
     * will be executed in a separate thread
     */
    public GLongTaskExecutor(boolean doInBackground) {
        this(doInBackground, "LongTaskExecutor");
    }

    /**
     * Execute a long task with cancel and progress support. Task can be
     * <code>null</code>. In this case
     * <code>runnable</code> will be executed normally, but without cancel and
     * progress support.
     *
     * @param task the task to be executed, can be <code>null</code>.
     * @param runnable the runnable to be executed
     * @param taskName the name of the task, is displayed in the status bar if
     * available
     * @param errorHandler error handler for exception retrieval during
     * execution
     * @throws NullPointerException if <code>runnable</code> *
     * or <code>taskName</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public void execute(GLongTask task, String taskName) {
        if (runningTask != null) {
            throw new IllegalStateException("A task is still executing");
        }
        if (executor == null) {
            this.executor = new ThreadPoolExecutor(0, 1, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory());
        }
        runningTask = new RunningLongTask(task,  taskName);
        if(this.interruptDelay > 0){
        	cancelTimer = new Timer(name + "_cancelTimer");
        	cancelTimer.schedule(new InterruptTimerTask(), interruptDelay);
        }
        if (inBackground) {
            runningTask.future = executor.submit(runningTask);
        } else {
            runningTask.run();            
        }
    }

    /**
     * Execute a long task with cancel and progress support. Task can be
     * <code>null</code>. In this case
     * <code>runnable</code> will be executed normally, but without cancel and
     * progress support.
     *
     * @param task the task to be executed, can be <code>null</code>.
     * @param runnable the runnable to be executed
     * @throws NullPointerException if <code>runnable</code> is null
     * @throws IllegalStateException if a task is still executing at this time
     */
    public void execute(GLongTask task) {
        execute(task, "");
    }

    /**
     * Cancel the current task. If the task fails to cancel itself and if an
     * <code>interruptDelay</code> has been specified, the task will be
     * <b>interrupted</b> after
     * <code>interruptDelay</code>. Using
     * <code>Thread.interrupt()</code> may cause hazardous behaviours and should
     * be avoided. Therefore any task should be cancellable.
     */
    public synchronized void cancel() {
        if (runningTask != null) {
            if (runningTask.isCancellable()) {
                if (interruptCancel) {
                    if (!runningTask.cancel()) {
                        cancelTimer = new Timer(name + "_cancelTimer");
                        cancelTimer.schedule(new InterruptTimerTask(), interruptDelay);
                    }
                } else {
                    runningTask.cancel();
                }
            }
        }
    }
    
    public boolean pause() throws InterruptedException{
    	if(runningTask != null){
    		return runningTask.pause();
    	}
    	return false;
    }
    
    public boolean resume() throws InterruptedException{
    	if(runningTask != null){
    		return runningTask.resume();
    	}
    	return false;
    }
    
    public float speed(){
    	if(runningTask != null){
    		return runningTask.speed();
    	}
    	return 0;
    }
    
    public float slow(float s){
    	if(runningTask != null){
    		return runningTask.slow(s);
    	}
    	return 0;
    }

    /**
     * Returns
     * <code>true</code> if the executor is executing a task.
     *
     * @return <code>true</code> if a task is running, <code>false</code>
     * otherwise
     */
    public boolean isRunning() {
        return runningTask != null;
    }

    private synchronized void finished() {
        if (cancelTimer != null) {
            cancelTimer.cancel();
        }
        runningTask = null;
    }

    /**
     * Inner class for associating a task to its Future instance
     */
    private class RunningLongTask implements Runnable {

        private final GLongTask task;
//        private final Runnable runnable;
        private Future<?> future;
        private boolean canceled;
        private boolean suspend;
        private float speed = 0;

        public RunningLongTask(GLongTask task, String taskName) {
            this.task = task;
        }

        @Override
        public void run() {
        	try {
        		task.init();
        		for (; (!canceled) && task.canGo();) {
        			if(suspend){
        				try {
        					Thread.sleep(50);
        					continue;
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        			}
        			if(speed > 0){
        				try {
        					Thread.sleep( (int)(50*speed) );
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        			}
        			task.go();
        		}
        		task.end();
            } catch (Exception e) {
                finished();
            }   
            
            finished();
        }

        public boolean cancel() {
            if (task != null) {
            	canceled = true;
            	task.end();
            }
            return true;
        }

        public boolean isCancellable() {
            if (inBackground) {
                if (!future.isCancelled()) {
                    return true;
                }
                return false;
            }
            return true;
        }
        
        public boolean pause(){
        	suspend = true;
        	return suspend;
        }
        
        public boolean resume(){
        	suspend = false;
        	return !suspend;
        }
        
        public float speed(){
        	return speed;
        }
        
        public float slow(float s){
        	speed = s;
        	return speed;
        }
    }

    /**
     * Inner class for naming the executor service thread
     */
    private class NamedThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }

    private class InterruptTimerTask extends TimerTask {

        @Override
        public void run() {
            if (runningTask != null) {
            	runningTask.cancel();
                cancelTimer = null;
                finished();
                executor.shutdownNow();
                executor = null;
            }
        }
    }
}
