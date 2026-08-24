export interface Publication {
  id: number
  title: string
  date: string | null
  doi: string | null
  authors: string
  type: string | null
  typeName: string | null
  vipAuthor: string | null
  vipApplication: string | null
}

export type PublicationInput = Omit<Publication, 'id' | 'vipAuthor'> & {
  id?: number
  vipAuthor?: string
}
