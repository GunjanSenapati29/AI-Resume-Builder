package com.skillgapai.dto;

/**
 * JSON response of POST /api/resumes/extract-text. characterCount lets
 * the caller show the user quick, visible confirmation that something
 * real was pulled out of the PDF, without them having to eyeball the
 * whole resumeText themselves.
 */
public record ExtractedTextResponse(String resumeText, int characterCount) {
}
