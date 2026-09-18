<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const containerRef = ref<HTMLDivElement | null>(null)
let renderer: THREE.WebGLRenderer | null = null
let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let globeGroup: THREE.Group | null = null
let stars: THREE.Points | null = null
let animId = 0

let isMouseDown = false
let mouseX = 0
let mouseY = 0
let targetRotX = 0
let targetRotY = 0
let rotX = 0
let rotY = 0

function lonLatToXY(lon: number, lat: number, w: number, h: number): [number, number] {
  const x = ((lon + 180) / 360) * w
  const y = ((90 - lat) / 180) * h
  return [x, y]
}

function drawContinent(ctx: CanvasRenderingContext2D, coords: [number, number][], w: number, h: number) {
  const pts = coords.map(([lon, lat]) => lonLatToXY(lon, lat, w, h))
  ctx.beginPath()
  ctx.moveTo(pts[0]![0], pts[0]![1])
  for (let i = 1; i < pts.length; i++) {
    ctx.lineTo(pts[i]![0], pts[i]![1])
  }
  ctx.closePath()
}

function createGridTexture(): THREE.CanvasTexture {
  const c = document.createElement('canvas')
  const w = 2048, h = 1024
  c.width = w
  c.height = h
  const ctx = c.getContext('2d')!

  ctx.fillStyle = '#060e1f'
  ctx.fillRect(0, 0, w, h)

  const continents: [number, number][][] = [
    [[-170,65],[-160,70],[-145,72],[-130,72],[-120,68],[-100,65],[-85,68],[-75,62],[-60,47],[-65,44],[-70,42],[-75,35],[-80,25],[-85,18],[-90,15],[-97,16],[-105,20],[-115,30],[-120,34],[-125,40],[-125,48],[-130,55],[-140,60],[-150,60],[-160,58],[-170,60]],
    [[-80,10],[-75,5],[-70,2],[-60,-3],[-50,-2],[-45,-5],[-38,-8],[-35,-12],[-38,-18],[-42,-23],[-48,-28],[-55,-34],[-60,-38],[-65,-42],[-70,-50],[-72,-52],[-70,-46],[-72,-40],[-70,-35],[-68,-28],[-70,-18],[-75,-10],[-77,-2],[-80,5]],
    [[-10,36],[0,38],[5,42],[0,47],[-5,48],[-10,52],[-5,55],[5,54],[10,55],[12,57],[18,55],[24,55],[28,52],[30,50],[32,46],[28,42],[25,38],[20,36],[15,38],[10,44],[5,46],[0,44],[-5,40],[-8,38]],
    [[-15,35],[-5,36],[10,37],[12,33],[15,30],[20,32],[25,32],[30,30],[35,30],[38,25],[42,12],[50,12],[48,8],[44,2],[40,-2],[38,-10],[35,-18],[32,-25],[30,-30],[28,-33],[25,-34],[20,-33],[18,-30],[15,-25],[12,-18],[10,-5],[5,0],[0,5],[-5,5],[-10,5],[-15,8],[-18,15],[-17,22],[-15,28]],
    [[28,42],[30,45],[35,42],[40,42],[45,40],[50,38],[55,25],[60,25],[65,25],[70,20],[75,12],[78,8],[80,12],[85,15],[90,22],[95,18],[100,15],[105,10],[110,5],[115,2],[120,5],[125,10],[130,15],[135,35],[140,38],[142,42],[145,45],[140,50],[135,55],[130,60],[120,65],[110,68],[100,70],[90,72],[80,70],[70,68],[60,65],[50,60],[40,55],[35,50],[30,48]],
    [[115,-15],[120,-13],[130,-12],[135,-13],[140,-15],[145,-15],[150,-18],[152,-22],[153,-28],[150,-33],[148,-38],[142,-38],[138,-35],[132,-32],[128,-30],[122,-28],[115,-25],[113,-22],[115,-18]],
    [[-55,60],[-45,60],[-35,65],[-20,70],[-18,76],[-20,80],[-30,82],[-45,82],[-55,80],[-60,75],[-55,68]],
    [[130,31],[132,33],[135,35],[138,37],[140,40],[142,43],[145,45],[143,43],[140,38],[137,34],[133,31],[130,30]],
    [[-6,50],[-3,51],[0,52],[2,53],[0,55],[-2,57],[-5,58],[-6,56],[-5,54],[-3,52]],
  ]

  ctx.fillStyle = 'rgba(6,182,212,0.06)'
  ctx.shadowColor = 'rgba(56,189,248,0.4)'
  ctx.shadowBlur = 6
  ctx.strokeStyle = 'rgba(6,182,212,0.5)'
  ctx.lineWidth = 1.5
  for (const pts of continents) {
    drawContinent(ctx, pts, w, h)
    ctx.fill()
    ctx.stroke()
  }
  ctx.shadowBlur = 0

  ctx.fillStyle = 'rgba(56,189,248,0.15)'
  for (const pts of continents) {
    drawContinent(ctx, pts, w, h)
    ctx.fill()
  }

  const cities: [number, number][] = [
    [116.4,39.9],[121.5,31.2],[113.3,23.1],[114.1,22.3],[120.2,30.3],
    [104.1,30.6],[106.5,29.6],[102.7,25.0],[108.9,34.3],[117.0,36.7],
    [-74.0,40.7],[-118.2,34.1],[-87.6,41.9],[-77.0,38.9],[-95.4,29.8],
    [-0.1,51.5],[2.3,48.9],[13.4,52.5],[12.5,41.9],[37.6,55.8],
    [139.7,35.7],[126.9,37.6],[103.8,1.4],[77.2,28.6],[51.4,35.7],
    [-43.2,-22.9],[-34.6,-15.8],[28.0,-26.2],[36.8,-1.3],[31.2,30.0],
  ]

  for (let i = 0; i < cities.length; i++) {
    const city = cities[i]!
    const lon = city[0]
    const lat = city[1]
    const xy = lonLatToXY(lon, lat, w, h)
    const x = xy[0]
    const y = xy[1]
    ctx.beginPath()
    ctx.arc(x, y, 2.5, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(56,189,248,0.9)'
    ctx.fill()
    ctx.beginPath()
    ctx.arc(x, y, 6, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(56,189,248,0.15)'
    ctx.fill()
  }

  const tex = new THREE.CanvasTexture(c)
  tex.wrapS = THREE.RepeatWrapping
  tex.wrapT = THREE.ClampToEdgeWrapping
  return tex
}

function createGlowTexture(): THREE.CanvasTexture {
  const c = document.createElement('canvas')
  c.width = 512
  c.height = 512
  const ctx = c.getContext('2d')!
  const gradient = ctx.createRadialGradient(256, 256, 128, 256, 256, 256)
  gradient.addColorStop(0, 'rgba(56,189,248,0.15)')
  gradient.addColorStop(0.4, 'rgba(56,189,248,0.06)')
  gradient.addColorStop(1, 'rgba(56,189,248,0)')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, 512, 512)
  return new THREE.CanvasTexture(c)
}

function createStarPositions(): Float32Array {
  const count = 2000
  const positions = new Float32Array(count * 3)
  const sizes = new Float32Array(count)
  for (let i = 0; i < count; i++) {
    const r = 30 + Math.random() * 40
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(2 * Math.random() - 1)
    positions[i * 3] = r * Math.sin(phi) * Math.cos(theta)
    positions[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta)
    positions[i * 3 + 2] = r * Math.cos(phi)
    sizes[i] = Math.random() * 1.5 + 0.3
  }
  return positions
}

function init() {
  if (!containerRef.value) return
  const w = containerRef.value.clientWidth
  const h = containerRef.value.clientHeight

  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(45, w / h, 0.1, 200)
  camera.position.z = 6

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setClearColor(0x000000, 0)
  containerRef.value.appendChild(renderer.domElement)

  globeGroup = new THREE.Group()
  scene.add(globeGroup)

  const ambientLight = new THREE.AmbientLight(0x334466, 0.8)
  scene.add(ambientLight)
  const dirLight = new THREE.DirectionalLight(0x88ccff, 1.2)
  dirLight.position.set(5, 3, 5)
  scene.add(dirLight)
  const rimLight = new THREE.DirectionalLight(0x38bdf8, 0.6)
  rimLight.position.set(-5, -2, -5)
  scene.add(rimLight)

  const gridTex = createGridTexture()
  const baseSphereGeo = new THREE.SphereGeometry(1.8, 64, 64)
  const baseSphereMat = new THREE.MeshPhongMaterial({
    map: gridTex,
    transparent: true,
    opacity: 1,
    shininess: 40,
    specular: new THREE.Color(0x1a3a5c),
    emissive: new THREE.Color(0x051428),
    emissiveIntensity: 0.4,
  })
  const baseSphere = new THREE.Mesh(baseSphereGeo, baseSphereMat)
  globeGroup.add(baseSphere)

  const wireGeo = new THREE.SphereGeometry(1.82, 48, 48)
  const wireMat = new THREE.MeshBasicMaterial({
    color: 0x38bdf8,
    wireframe: true,
    transparent: true,
    opacity: 0.12,
  })
  const wireSphere = new THREE.Mesh(wireGeo, wireMat)
  globeGroup.add(wireSphere)

  const glowTex = createGlowTexture()
  const glowGeo = new THREE.SphereGeometry(2.15, 64, 64)
  const glowMat = new THREE.MeshBasicMaterial({
    map: glowTex,
    transparent: true,
    opacity: 0.8,
    side: THREE.FrontSide,
    depthWrite: false,
    blending: THREE.AdditiveBlending,
  })
  const glowSphere = new THREE.Mesh(glowGeo, glowMat)
  globeGroup.add(glowSphere)

  const innerGlowGeo = new THREE.SphereGeometry(1.95, 64, 64)
  const innerGlowMat = new THREE.MeshBasicMaterial({
    color: 0x38bdf8,
    transparent: true,
    opacity: 0.04,
    side: THREE.BackSide,
    depthWrite: false,
    blending: THREE.AdditiveBlending,
  })
  const innerGlow = new THREE.Mesh(innerGlowGeo, innerGlowMat)
  globeGroup.add(innerGlow)

  const starPositions = createStarPositions()
  const starGeo = new THREE.BufferGeometry()
  starGeo.setAttribute('position', new THREE.BufferAttribute(starPositions, 3))
  const starMat = new THREE.PointsMaterial({
    color: 0xffffff,
    size: 0.12,
    transparent: true,
    opacity: 0.7,
    sizeAttenuation: true,
    depthWrite: false,
  })
  stars = new THREE.Points(starGeo, starMat)
  scene.add(stars)
}

function animate() {
  animId = requestAnimationFrame(animate)
  if (!globeGroup || !scene || !camera || !renderer || !stars) return

  if (!isMouseDown) {
    targetRotY += 0.002
  }
  rotX += (targetRotX - rotX) * 0.05
  rotY += (targetRotY - rotY) * 0.05

  globeGroup.rotation.x = rotX
  globeGroup.rotation.y = rotY

  stars.rotation.y += 0.0002
  stars.rotation.x += 0.0001

  renderer.render(scene, camera)
}

function onResize() {
  if (!containerRef.value || !camera || !renderer) return
  const w = containerRef.value.clientWidth
  const h = containerRef.value.clientHeight
  camera.aspect = w / h
  camera.updateProjectionMatrix()
  renderer.setSize(w, h)
}

function onMouseDown(e: MouseEvent) {
  isMouseDown = true
  mouseX = e.clientX
  mouseY = e.clientY
}

function onMouseMove(e: MouseEvent) {
  if (!isMouseDown) return
  const dx = e.clientX - mouseX
  const dy = e.clientY - mouseY
  targetRotY += dx * 0.005
  targetRotX += dy * 0.005
  targetRotX = Math.max(-Math.PI / 3, Math.min(Math.PI / 3, targetRotX))
  mouseX = e.clientX
  mouseY = e.clientY
}

function onMouseUp() {
  isMouseDown = false
}

onMounted(() => {
  init()
  animate()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  cancelAnimationFrame(animId)
  window.removeEventListener('resize', onResize)
  if (renderer) {
    renderer.dispose()
    renderer.domElement.remove()
  }
  if (scene) {
    scene.traverse((obj) => {
      if (obj instanceof THREE.Mesh) {
        obj.geometry.dispose()
        if (Array.isArray(obj.material)) {
          obj.material.forEach((m) => m.dispose())
        } else {
          obj.material.dispose()
        }
      }
      if (obj instanceof THREE.Points) {
        obj.geometry.dispose()
        if (obj.material instanceof THREE.Material) obj.material.dispose()
      }
    })
  }
  renderer = null
  scene = null
  camera = null
  globeGroup = null
  stars = null
})
</script>

<template>
  <div
    ref="containerRef"
    class="globe-container"
    @mousedown="onMouseDown"
    @mousemove="onMouseMove"
    @mouseup="onMouseUp"
    @mouseleave="onMouseUp"
  ></div>
</template>

<style scoped>
.globe-container {
  width: 100%;
  height: 100%;
  cursor: grab;
  position: relative;
}
.globe-container:active {
  cursor: grabbing;
}
</style>
