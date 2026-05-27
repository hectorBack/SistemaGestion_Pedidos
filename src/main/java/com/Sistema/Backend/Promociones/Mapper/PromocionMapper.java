package com.Sistema.Backend.Promociones.Mapper;

import com.Sistema.Backend.Promociones.Dto.Request.PromocionRequestDTO;
import com.Sistema.Backend.Promociones.Dto.Response.PromocionResponseDTO;
import com.Sistema.Backend.Productos.Entity.Producto;
import com.Sistema.Backend.Promociones.Entity.Promocion;
import com.Sistema.Backend.Exception.ResourceNotFoundException;
import com.Sistema.Backend.Productos.Repository.ProductoRepository;
import org.springframework.stereotype.Component;

@Component
public class PromocionMapper {

    private final ProductoRepository productoRepository;

    public PromocionMapper(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Promocion toEntity(PromocionRequestDTO dto){
        if (dto == null) return null;

        Promocion promocion = new Promocion();
        promocion.setNombre(dto.getNombre());
        promocion.setDescripcion(dto.getDescripcion());
        promocion.setTipoDescuento(dto.getTipoDescuento().toUpperCase());
        promocion.setValor(dto.getValor());
        promocion.setFechaInicio(dto.getFechaInicio());
        promocion.setFechaFin(dto.getFechaFin());
        promocion.setActiva(dto.isActiva());

        if (dto.getProductoId() != null){
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + dto.getProductoId()));
            promocion.setProducto(producto);
        }
        return promocion;
    }

    public PromocionResponseDTO toResponseDTO(Promocion entity) {
        if (entity == null) return null;

        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setTipoDescuento(entity.getTipoDescuento());
        dto.setValor(entity.getValor());
        dto.setFechaInicio(entity.getFechaInicio());
        dto.setFechaFin(entity.getFechaFin());
        dto.setActiva(entity.isActiva());

        if (entity.getProducto() != null) {
            dto.setProductoId(entity.getProducto().getId());
            dto.setNombreProducto(entity.getProducto().getNombre());
        }

        return dto;
    }
}
