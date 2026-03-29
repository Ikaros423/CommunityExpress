package com.express.system.dto;

import java.util.List;

public class DashboardRanksVO {

    private List<ShelfLoadVO> topLoadShelves;
    private List<OverdueExpressVO> topOverdueExpresses;

    public List<ShelfLoadVO> getTopLoadShelves() {
        return topLoadShelves;
    }

    public void setTopLoadShelves(List<ShelfLoadVO> topLoadShelves) {
        this.topLoadShelves = topLoadShelves;
    }

    public List<OverdueExpressVO> getTopOverdueExpresses() {
        return topOverdueExpresses;
    }

    public void setTopOverdueExpresses(List<OverdueExpressVO> topOverdueExpresses) {
        this.topOverdueExpresses = topOverdueExpresses;
    }
}
