package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 20: JSON shape of GET /api/reports/role-recommendations - one
 * entry per role in the catalog, sorted by fit score descending. Unlike
 * Phase 17/19's "latest report" views, this is aggregated across ALL of
 * the user's GapReports, so there's no single reportId driving it.
 */
public record RoleRecommendationsView(List<RoleFitView> roles) {
}
