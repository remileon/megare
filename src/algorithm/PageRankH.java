package algorithm;

import api.Algorithm;
import model.AccumDouble;
import model.NodeWithDegreeDouble;
import model.UpdateDouble;

/**
 * Created by yibai on 2016/5/19.
 */
public class PageRankH implements Algorithm<NodeWithDegreeDouble, AccumDouble, UpdateDouble> {

    @Override
    public UpdateDouble scatter(UpdateDouble u, NodeWithDegreeDouble n) {
        u.value = n.value / n.degree;
        u.value = u.value / n.degree;
        u.value = u.value / n.degree;
        u.value = u.value / n.degree;
        u.value = u.value / n.degree;
        u.value = u.value / n.degree;
        u.value = u.value / n.degree;
        u.value = u.value / n.degree;
        u.value = u.value / n.degree;
        return u;
    }

    @Override
    public AccumDouble gather(AccumDouble a, UpdateDouble u) {
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        a.value = a.value + u.value;
        return a;
    }

    @Override
    public NodeWithDegreeDouble apply(NodeWithDegreeDouble n, AccumDouble a) {
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        n.value = n.value * 0.15 + a.value * 0.85;
        return n;
    }
}
