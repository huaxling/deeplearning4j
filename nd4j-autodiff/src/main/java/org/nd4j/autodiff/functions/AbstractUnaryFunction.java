package org.nd4j.autodiff.functions;

import lombok.Data;
import org.nd4j.autodiff.ArrayField;
import org.nd4j.autodiff.Field;
import org.nd4j.autodiff.graph.Graph;
import org.nd4j.autodiff.opstate.NDArrayInformation;
import org.nd4j.autodiff.opstate.NDArrayVertex;
import org.nd4j.autodiff.opstate.OpState;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.List;

@Data
public abstract class AbstractUnaryFunction<X extends Field<X>> extends DifferentialFunction<X> {

    protected DifferentialFunction<X> m_x;
    protected int[] shape;

    public AbstractUnaryFunction(Graph<NDArrayInformation,OpState> graph, DifferentialFunction<X> i_v,int[] shape) {
        super(graph);
        if (i_v != null) {
            m_x = i_v;
            addEdges(graph,m_x,functionName(),shape);
        } else {
            throw new IllegalArgumentException("Input not null variable.");
        }
    }


    public AbstractUnaryFunction(Graph<NDArrayInformation,OpState> graph, DifferentialFunction<X> i_v) {
        super(graph);
        if (i_v != null) {
            m_x = i_v;
            addEdges(graph,m_x,functionName());
        } else {
            throw new IllegalArgumentException("Input not null variable.");
        }
    }


    @Override
    public String doGetFormula(List<Variable<X>> variables) {
        return functionName() + "(" + arg().doGetFormula(variables) + ")";
    }

    @Override
    public String toString() {
        return functionName() + "(" + arg().toString() + ")";
    }

    /**
     * Add nodes to the graph
     * @param graph
     * @param i_v1
     * @param opName
     */
    protected void addEdges(Graph<NDArrayInformation,OpState> graph, DifferentialFunction<X> i_v1,String opName,int...shape) {
        if(i_v1.getValue() instanceof ArrayField) {
            ArrayField v1 = (ArrayField) i_v1.getValue();
            NDArrayInformation information =    NDArrayInformation.builder()
                    .id(opName + "(" + v1.getInput().getId() + " -> " + v1.getInput().getId() + ")")
                    .shape(shape).build();
            //result
            NDArrayVertex newVertex = new NDArrayVertex(graph.getVertices().size(),information);
            this.vertexId = newVertex.vertexID();

            graph.addVertex(newVertex);
            OpState owner =  OpState.builder()
                    .opType(OpState.OpType.TRANSFORM)
                    .opName(opName)
                    .id(opName + "(" + v1.getInput().getId() + " -> " + newVertex.getValue().getId() + ")")
                    .vertexIds(new String[]{String.valueOf(v1.getVertex().vertexID()),String.valueOf(newVertex.vertexID())})
                    .n(ArrayUtil.prod(shape))
                    .build();
            graph.addEdge(v1.getVertex().vertexID(),newVertex.vertexID(),owner,true);
            newVertex.setOpState(owner);
            information.setOwner(owner);
            owner.setResult(information);
            this.opState = owner;

        }
    }


    /**
     * Add nodes to the graph
     * @param graph
     * @param i_v1
     * @param opName
     */
    protected void addEdges(Graph<NDArrayInformation,OpState> graph, DifferentialFunction<X> i_v1,String opName) {
        if(i_v1.getValue() instanceof ArrayField) {
            ArrayField arrayField = (ArrayField) i_v1.getValue();
            addEdges(graph,i_v1,opName,arrayField.getInput().getShape());

        }
    }

    @Override
    public DifferentialFunction<X>[] args() {
        return new DifferentialFunction[] {arg()};
    }

    public DifferentialFunction<X> arg() {
        return m_x;
    }


    @Override
    public DifferentialFunction<X> dup() {
        try {
            return getClass().getConstructor(graph.getClass(),arg().getClass(),int[].class).newInstance(graph,arg(),shape);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

