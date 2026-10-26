import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PaginationBar from '@/components/PaginationBar.vue'
import PokemonCard from '@/components/PokemonCard.vue'
import EvolutionTree from '@/components/EvolutionTree.vue'
import StatBar from '@/components/StatBar.vue'

const RouterLinkStub = { props: ['to'], template: '<a><slot /></a>' }
const stubs = { RouterLink: RouterLinkStub }

describe('PaginationBar', () => {
  it('emits the requested page and disables Prev on the first page', async () => {
    const wrapper = mount(PaginationBar, { props: { page: 1, totalPages: 10 } })

    const prev = wrapper.findAll('button').find((b) => b.text().includes('Prev'))
    expect(prev?.attributes('disabled')).toBeDefined()

    await wrapper.findAll('button').find((b) => b.text().includes('Next'))?.trigger('click')
    expect(wrapper.emitted('change')?.[0]).toEqual([2])

    await wrapper.find('button[aria-label="Page 3"]').trigger('click')
    expect(wrapper.emitted('change')?.[1]).toEqual([3])
  })

  it('renders nothing when there is a single page', () => {
    const wrapper = mount(PaginationBar, { props: { page: 1, totalPages: 1 } })
    expect(wrapper.find('nav').exists()).toBe(false)
  })

  it('marks the current page for assistive tech', () => {
    const wrapper = mount(PaginationBar, { props: { page: 4, totalPages: 20 } })
    expect(wrapper.find('[aria-current="page"]').text()).toBe('4')
  })
})

describe('PokemonCard', () => {
  it('shows name, category, weight in kg and abilities', () => {
    const wrapper = mount(PokemonCard, {
      global: { stubs },
      props: {
        pokemon: {
          id: 25,
          name: 'pikachu',
          sprite: 'https://example.com/25.png',
          category: 'Mouse Pokémon',
          weight: 60,
          abilities: ['static', 'lightning-rod'],
        },
      },
    })

    expect(wrapper.text()).toContain('Pikachu')
    expect(wrapper.text()).toContain('Mouse Pokémon')
    expect(wrapper.text()).toContain('6.0 kg')
    expect(wrapper.text()).toContain('Lightning Rod')
    expect(wrapper.find('img').attributes('alt')).toBe('Pikachu sprite')
  })
})

describe('EvolutionTree', () => {
  it('renders a branching evolution chain', () => {
    const wrapper = mount(EvolutionTree, {
      global: { stubs },
      props: {
        currentId: 133,
        node: {
          id: 133,
          name: 'eevee',
          evolvesTo: [
            { id: 134, name: 'vaporeon', evolvesTo: [] },
            { id: 135, name: 'jolteon', evolvesTo: [] },
          ],
        },
      },
    })

    const text = wrapper.text()
    expect(text).toContain('Eevee')
    expect(text).toContain('Vaporeon')
    expect(text).toContain('Jolteon')
    expect(wrapper.findAll('img')).toHaveLength(3)
  })
})

describe('StatBar', () => {
  it('exposes the value to assistive tech and scales the bar', () => {
    const wrapper = mount(StatBar, { props: { name: 'hp', value: 128 } })
    const bar = wrapper.find('[role="progressbar"]')
    expect(bar.attributes('aria-valuenow')).toBe('128')
    expect(wrapper.text()).toContain('HP')
    expect(wrapper.find('.stat-fill').attributes('style')).toContain('width: 50%')
  })
})
