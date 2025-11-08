package com.sapiens.innovate.vo;
import java.util.List;

public class PaginatedClaimResponse {
    private List<ClaimDTO> claims;
    private long totalRecords;
    private int currentPage;
    private int pageSize;
    private int totalPages;

    public PaginatedClaimResponse() {}

    public PaginatedClaimResponse(List<ClaimDTO> claims, long totalRecords, int currentPage, int pageSize) {
        this.claims = claims;
        this.totalRecords = totalRecords;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    }

    public List<ClaimDTO> getClaims() {
        return claims;
    }

    public void setClaims(List<ClaimDTO> claims) {
        this.claims = claims;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
