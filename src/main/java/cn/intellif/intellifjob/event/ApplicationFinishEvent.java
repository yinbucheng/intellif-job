package cn.intellif.intellifjob.event;

import org.springframework.context.ApplicationEvent;

/**
 * author 尹冲
 */
public class ApplicationFinishEvent extends ApplicationEvent {

    public ApplicationFinishEvent(Object source) {
        super(source);
    }
}
