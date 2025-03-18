package beetrap.btfmc;

@FunctionalInterface
public interface Distance<T> {
    double distance(T t1, T t2);
}
