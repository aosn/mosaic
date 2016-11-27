package io.github.aosn.mosaic.ui.view.table;

import com.vaadin.data.util.BeanItemContainer;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * @author mikan
 * @since 0.1
 */
@Getter
@Builder
public class VoteRow implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private transient final Vote entity;
    private final String user;
    private final String time;
    private final String book;

    public static VoteRow from(Vote entity) {
        return VoteRow.builder()
                .entity(entity)
                .user(entity.getUser().getName())
                .time(DATE_FORMAT.format(entity.getDate()))
                .book(entity.getBook().getGitHubIssue().getTitle())
                .build();
    }

    public static BeanItemContainer<VoteRow> toContainer(List<VoteRow> rows) {
        if (rows == null) {
            rows = Collections.emptyList();
        }
        return new BeanItemContainer<>(VoteRow.class, rows);
    }
}
