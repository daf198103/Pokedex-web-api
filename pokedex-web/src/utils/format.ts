export function formatName(name: string): string {
  return name
    .split('-')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

/** hectograms -> kilograms */
export const toKg = (hectograms: number): string => (hectograms / 10).toFixed(1)

/** decimetres -> metres */
export const toMetres = (decimetres: number): string => (decimetres / 10).toFixed(1)

export const spriteUrl = (id: number): string =>
  `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${id}.png`

export function formatDate(iso: string | null | undefined): string {
  if (!iso) return '-'
  const date = new Date(iso)
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString()
}
