package api;

import model.Savable;

/**
 * Created by yibai on 2016/3/15.
 */
public interface Algorithm<Node extends Savable, Accum, Update extends Savable> {
    public Update scatter(Update u, Node n);
    public Accum gather(Accum a, Update u);
    public Node apply(Node n, Accum a);
}
