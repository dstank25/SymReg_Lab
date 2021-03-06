package hr.fer.zemris.ecf.symreg.model.exp;

import hr.fer.zemris.ecf.lab.engine.console.Job;
import hr.fer.zemris.ecf.lab.engine.log.LogModel;
import hr.fer.zemris.ecf.lab.engine.task.JobListener;
import hr.fer.zemris.ecf.symreg.model.util.FitnessSizeLog;
import hr.fer.zemris.ecf.symreg.model.util.HallOfFameUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dstankovic on 4/27/16.
 */
public class ParallelSRManager implements JobListener {
  private ExperimentInput experimentInput;
  private ParallelExperimentsListener listener;
  private int threads;

  private ExecutorService service;
  private boolean stopped = false;
  private int logProcessCount;
  private boolean started = false;
  private List<LogModel> logs;

  public ParallelSRManager(ParallelExperimentsListener listener, int threads) {
    this.listener = listener;
    this.threads = threads;
  }

  public ExecutorService getService() {
    if (service == null) {
      service = Executors.newFixedThreadPool(threads);
    }
    return service;
  }

  public void run(ExperimentInput experimentInput) {
    this.experimentInput = experimentInput;
    stopped = false;
    restart();

    for (int i = 0; i < threads; i++) {
      invokeNewExperiment();
    }
  }

  private void restart() {
    logs = new ArrayList<>(threads);
    logProcessCount = 0;
  }

  public void stop() {
    stopped = true;
    if (service != null) {
      service.shutdown();
      service = null;
    }
  }

  private void invokeNewExperiment() {
    if (!stopped) {
      invokeTask(() -> runNewExperiment());
    }
  }

  private void runNewExperiment() {
    SRManager manager = new SRManager(this);
    manager.run(experimentInput);
  }

  private void invokeTask(Runnable task) {
    getService().execute(task);
  }

  private List<LogModel> extractParetoFrontier() {
    List<FitnessSizeLog> list = HallOfFameUtils.extractParetoFrontier(logs);

    Set<LogModel> set = new HashSet<>(list.size());
    List<LogModel> paretoFrontier = new ArrayList<>(list.size());
    for (FitnessSizeLog fitnessSizeLog : list) {
      set.add(fitnessSizeLog.logModel);
      paretoFrontier.add(fitnessSizeLog.logModel);
    }
    logs.retainAll(set); // remove obsolete logs

    return paretoFrontier;
  }

  private void processNewLog(LogModel logModel) {
    if (!stopped) {
      logs.add(logModel);
      logProcessCount++;
      if (logProcessCount % threads == 0) {
        listener.experimentsUpdated(extractParetoFrontier());
      }
    }
  }

  @Override
  public void jobInitialized(Job job) {
  }

  @Override
  public void jobStarted(Job job) {
    synchronized (this) {
      if (!started) {
        listener.experimentsStarted();
        started = true;
      }
    }
  }

  @Override
  public void jobPartiallyFinished(Job job, LogModel logModel) {
    synchronized (this) {
      processNewLog(logModel);
    }
  }

  @Override
  public void jobFinished(Job job, LogModel logModel) {
    synchronized (this) {
      processNewLog(logModel);
      invokeNewExperiment();
    }
  }

  @Override
  public void jobFailed(Job job) {
    synchronized (this) {
      invokeNewExperiment();
    }
  }
}
