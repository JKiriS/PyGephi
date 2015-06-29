package org.pygephi.core;

import java.util.HashMap;
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
    private final long interruptDelay;
    private final String name;
    private RunningLongTask runningTask;
    private Timer cancelTimer;
    private ExecutorService executor;
    HashMap<String, GLongTaskExecutor> longTasks;

    /**
     * Creates a new long task executor.
     *
     * @param doInBackground when <code>true</code>, the task will be executed
     * in a separate thread
     * @param name the name of the executor, used to recognize threads by names
     * @param interruptDelay number of seconds to wait before *
     * calling <code>Thread.interrupt()</code> after a cancel request
     */
    public GLongTaskExecutor(GLongTask task, boolean doInBackground, String taskName, float interruptDelay) {
        this.inBackground = doInBackground;
        this.name = taskName;
        this.interruptDelay = (long) interruptDelay * 1000;
        
        if (runningTask != null) {
            throw new IllegalStateException("A task is still executing");
        }
        if (executor == null) {
            this.executor = new ThreadPoolExecutor(0, 1, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory());
        }
        runningTask = new RunningLongTask(task,  taskName);
    }
    
    public GLongTaskExecutor(GLongTask task, boolean doInBackground, String name) {
        this(task, doInBackground, name, 0);
    }

    public GLongTaskExecutor(GLongTask task, boolean doInBackground) {
        this(task, doInBackground, "LongTaskExecutor");
    }
    
    public String getName(){
    	return this.name;
    }
    
    public void setLongTaskPool(HashMap<String, GLongTaskExecutor> longTasks){
    	this.longTasks = longTasks;
    }

    public synchronized void cancel() {
        if (runningTask != null) {
            if (runningTask.isCancellable()) {
                runningTask.cancel();
                finished();
            }
        }
    }
    
    public boolean start(){
    	if(runningTask != null){
    		if(this.interruptDelay > 0){
            	cancelTimer = new Timer(name + "_cancelTimer");
            	cancelTimer.schedule(new InterruptTimerTask(), this.interruptDelay);
            }
    		if (inBackground) {
                runningTask.future = executor.submit(runningTask);
            } else {
                runningTask.run();            
            }
    		return true;
    	}
    	return false;
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
    
    public float slow(int s){
    	if(runningTask != null){
    		return runningTask.slow(s);
    	}
    	return 0;
    }
    
    public void step(){
    	if(runningTask != null){
    		runningTask.step();
    	}
    }

    public boolean isRunning() {
        return runningTask != null;
    }

    private synchronized void finished() {
        if (cancelTimer != null) {
            cancelTimer.cancel();
        }
        runningTask = null;
        if(longTasks != null)
        	longTasks.remove(this.name);
    }

    /**
     * Inner class for associating a task to its Future instance
     */
    private class RunningLongTask implements Runnable {

        private final GLongTask task;
        private Future<?> future;
        private boolean canceled;
        private boolean suspend;
        private int speed = 0;
        private int defaultsleep = 50;
        private boolean step = false;

        public RunningLongTask(GLongTask task, String taskName) {
            this.task = task;
        }

        @Override
        public void run() {
        	try {
        		task.init();
        		for (; (!canceled) && task.canGo();) {
        			
        			if(step){
        				suspend = true;
        				step = false;
        				task.go();
        			}
        			
        			if(suspend){
        				try {
        					Thread.sleep(defaultsleep);
        					continue;
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        			}
        			
        			if(speed > 0){
        				try {
        					int temp = speed;
        					while(temp-- > 0 && temp < speed)
        						Thread.sleep( defaultsleep );
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        			}
        			task.go();
        		}
        		task.end();
            } catch (Exception e) {
            	e.getStackTrace();
                finished();
            }   
            
            finished();
        }
        
        public void step(){
        	this.step = true;
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
        	step = false;
        	return !suspend;
        }
        
        public int speed(){
        	return speed;
        }
        
        public int slow(int s){
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
