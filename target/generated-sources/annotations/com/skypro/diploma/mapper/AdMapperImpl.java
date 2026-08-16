package com.skypro.diploma.mapper;

import com.skypro.diploma.dto.ad.AdDto;
import com.skypro.diploma.dto.ad.CreateAdReq;
import com.skypro.diploma.dto.ad.FullAdDto;
import com.skypro.diploma.dto.ad.UpdateAdReq;
import com.skypro.diploma.entity.Ad;
import com.skypro.diploma.entity.User;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 18.0.2.1 (Oracle Corporation)"
)
@Component
public class AdMapperImpl implements AdMapper {

    private final DateTimeFormatter dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168 = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

    @Override
    public AdDto toDto(Ad ad) {
        if ( ad == null ) {
            return null;
        }

        AdDto adDto = new AdDto();

        adDto.setAuthor( adAuthorId( ad ) );
        adDto.setImage( ad.getImagePath() );
        if ( ad.getCreatedAt() != null ) {
            adDto.setCreatedAt( dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168.format( ad.getCreatedAt() ) );
        }
        adDto.setId( ad.getId() );
        adDto.setTitle( ad.getTitle() );
        adDto.setPrice( ad.getPrice() );
        adDto.setDescription( ad.getDescription() );

        return adDto;
    }

    @Override
    public FullAdDto toFullAdDto(Ad ad) {
        if ( ad == null ) {
            return null;
        }

        FullAdDto fullAdDto = new FullAdDto();

        fullAdDto.setAuthor( adAuthorId( ad ) );
        fullAdDto.setAuthorName( mapUserToAuthorName( ad.getAuthor() ) );
        fullAdDto.setImage( ad.getImagePath() );
        if ( ad.getCreatedAt() != null ) {
            fullAdDto.setCreatedAt( dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168.format( ad.getCreatedAt() ) );
        }
        fullAdDto.setId( ad.getId() );
        fullAdDto.setTitle( ad.getTitle() );
        fullAdDto.setPrice( ad.getPrice() );
        fullAdDto.setDescription( ad.getDescription() );

        return fullAdDto;
    }

    @Override
    public Ad toEntity(CreateAdReq createAdReq) {
        if ( createAdReq == null ) {
            return null;
        }

        Ad.AdBuilder ad = Ad.builder();

        ad.title( createAdReq.getTitle() );
        ad.description( createAdReq.getDescription() );
        ad.price( createAdReq.getPrice() );

        return ad.build();
    }

    @Override
    public void updateAdFromDto(UpdateAdReq updateAdReq, Ad ad) {
        if ( updateAdReq == null ) {
            return;
        }

        if ( updateAdReq.getTitle() != null ) {
            ad.setTitle( updateAdReq.getTitle() );
        }
        if ( updateAdReq.getDescription() != null ) {
            ad.setDescription( updateAdReq.getDescription() );
        }
        if ( updateAdReq.getPrice() != null ) {
            ad.setPrice( updateAdReq.getPrice() );
        }
    }

    @Override
    public List<AdDto> toDtoList(List<Ad> ads) {
        if ( ads == null ) {
            return null;
        }

        List<AdDto> list = new ArrayList<AdDto>( ads.size() );
        for ( Ad ad : ads ) {
            list.add( toDto( ad ) );
        }

        return list;
    }

    private Long adAuthorId(Ad ad) {
        if ( ad == null ) {
            return null;
        }
        User author = ad.getAuthor();
        if ( author == null ) {
            return null;
        }
        Long id = author.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
