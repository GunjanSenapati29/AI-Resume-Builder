const dateFormatter = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' })

// Shared by GapReportScreen (report-header "Analyzed ...") and
// HistoryScreen (each row's date) so both use the exact same format.
export function formatDate(isoString) {
  return dateFormatter.format(new Date(isoString))
}
