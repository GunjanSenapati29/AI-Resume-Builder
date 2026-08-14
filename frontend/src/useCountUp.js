import { useEffect, useState } from 'react'

/**
 * Phase 11: animates a number counting up to `target` whenever it
 * changes - ease-out cubic, mirrors design-reference.html's animateCount.
 * Shared by MatchMeter's percentage and its three skill counts so all
 * four don't each hand-roll the same requestAnimationFrame loop.
 * Respects prefers-reduced-motion (jumps straight to the target).
 */
export function useCountUp(target, duration = 900) {
  const [value, setValue] = useState(0)

  useEffect(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      setValue(target)
      return
    }

    let frame
    const start = performance.now()

    function tick(now) {
      const progress = Math.min(1, (now - start) / duration)
      const eased = 1 - Math.pow(1 - progress, 3)
      setValue(Math.round(target * eased))
      if (progress < 1) {
        frame = requestAnimationFrame(tick)
      }
    }

    frame = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frame)
  }, [target, duration])

  return value
}
