import {useRef} from 'react'

interface Props extends React.HTMLAttributes<HTMLDivElement> {
    children: React.ReactNode
}

/**
 * 마우스 드래그로 가로 스크롤이 가능한 컨테이너.
 * 모바일 터치는 기본 동작에 맡기고, 데스크탑에서 클릭-드래그를 처리합니다.
 */
function DragScrollContainer({children, ...rest}: Props) {
    const ref = useRef<HTMLDivElement>(null)
    const isDragging = useRef(false)
    const hasDragged = useRef(false)
    const startX = useRef(0)
    const scrollLeft = useRef(0)

    const handleMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
        if (!ref.current) return
        isDragging.current = true
        hasDragged.current = false
        startX.current = e.pageX - ref.current.offsetLeft
        scrollLeft.current = ref.current.scrollLeft
        ref.current.style.cursor = 'grabbing'
    }

    const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
        if (!isDragging.current || !ref.current) return
        e.preventDefault()
        const x = e.pageX - ref.current.offsetLeft
        const walk = x - startX.current
        if (Math.abs(walk) > 4) hasDragged.current = true
        ref.current.scrollLeft = scrollLeft.current - walk
    }

    const stopDrag = () => {
        if (!ref.current) return
        isDragging.current = false
        ref.current.style.cursor = ''
    }

    const handleClickCapture = (e: React.MouseEvent<HTMLDivElement>) => {
        if (hasDragged.current) {
            e.stopPropagation()
            hasDragged.current = false
        }
    }

    return (
        <div
            ref={ref}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={stopDrag}
            onMouseLeave={stopDrag}
            onDragStart={(e) => e.preventDefault()}
            onClickCapture={handleClickCapture}
            {...rest}
        >
            {children}
        </div>
    )
}

export default DragScrollContainer
