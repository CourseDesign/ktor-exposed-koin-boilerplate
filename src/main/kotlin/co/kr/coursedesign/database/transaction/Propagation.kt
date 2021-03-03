package co.kr.coursedesign.database.transaction

enum class Propagation {
    MANDATORY,
    NESTED,
    NEVER, // TODO(지원)
    NOT_SUPPORTED, // TODO(지원)
    REQUIRED,
    REQUIRES_NEW,
    SUPPORTS, // TODO(지원)
}
