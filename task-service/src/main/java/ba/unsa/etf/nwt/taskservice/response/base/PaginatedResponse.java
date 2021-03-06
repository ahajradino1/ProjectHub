package ba.unsa.etf.nwt.taskservice.response.base;

import ba.unsa.etf.nwt.taskservice.response.interfaces.Metadata;
import ba.unsa.etf.nwt.taskservice.response.interfaces.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T extends Resource, MT extends Metadata> {
    private MT metadata;
    private List<T> data;
}
