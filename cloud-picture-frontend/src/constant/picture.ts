export const PIC_REVIEW_STATUS_ENUM = {
  REVIEWING: 0,
  ACCEPT: 1,
  REJECT: 2
}

export const PIC_REVIEW_STATUS_MAP: Record<number, string> = {
  0: '待审核',
  1: '通过',
  2: '拒绝'
}

export const PIC_REVIEW_STATUS_OPTIONS = Object.keys(PIC_REVIEW_STATUS_MAP).map((key: string) => {
  const value = Number(key);
  return {
    label: PIC_REVIEW_STATUS_MAP[value],
    value: value
  }
})

