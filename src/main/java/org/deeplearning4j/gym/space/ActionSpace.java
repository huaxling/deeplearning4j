package org.deeplearning4j.gym.space;

/**
 * Created by rubenfiszel on 7/8/16.
 */
abstract public class ActionSpace<T> {

    abstract String getInfoName();

    abstract public T randomAction();

}
