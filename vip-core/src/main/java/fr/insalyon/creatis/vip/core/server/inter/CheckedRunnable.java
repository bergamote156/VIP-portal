package fr.insalyon.creatis.vip.core.server.inter;

// this is useful to execute Runnable with code
// that throws explicit Exceptions
// otherwise Java won't compile, see: https://stackoverflow.com/questions/11584159/is-there-a-way-to-make-runnables-run-throw-an-exception
@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {
    void run() throws E;
}
