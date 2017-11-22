package ownradio.domain;

import lombok.Getter;
import lombok.Setter;
import ownradio.web.rest.v4.HistoryController;

import java.util.List;

/**
 * Created by a.polunina on 25.05.2017.
 */

@Getter
@Setter
public class HistoryArray {

	private List<HistoryDTO> historyList;

}


